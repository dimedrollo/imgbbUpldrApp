package ru.dopegeek.imgbbupldr;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import ru.dopegeek.imgbbupldr.parameter.ExpirationTime;
import ru.dopegeek.imgbbupldr.parameter.ExpirationTimeFragment;
import ru.dopegeek.imgbbupldr.parameter.UploadParameters;
import ru.dopegeek.imgbbupldr.response.OptionalResponse;

import static android.app.Activity.RESULT_OK;
import static ru.dopegeek.imgbbupldr.parameter.ExpirationTimeFragment.expTimeValue;

public class ImgbbUpFragment extends Fragment {


    private final int pick_image = 1;
    public UploadParameters params;
    MenuItem button;
    private RecyclerView mImgbbRecyclerView;
    private ImgAdapter mImgAdapter;
    private Uri mImageUri;
    private Bitmap selectedImage;

    public static ImgbbUpFragment newInstance() {
        return new ImgbbUpFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.imgbb_fragment, container,
                false);
        mImgbbRecyclerView = v.findViewById(R.id.imgbb_recycler_view);
        mImgbbRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_add, menu);
    }

    private void updateUI() {
        ImgLab imgLab = ImgLab.get(getActivity());
        List<Img> imgs = imgLab.getImgs();

        if (mImgAdapter == null) {
            mImgAdapter = new ImgAdapter(imgs);
            mImgbbRecyclerView.setAdapter(mImgAdapter);
        } else {
            mImgAdapter.setImgs(imgs);
            mImgAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_img:
                button = item;
                button.setEnabled(false);
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, pick_image);
                return true;
            case R.id.set_timer:
                ExpirationTimeFragment expirationTimeFragment = new ExpirationTimeFragment();
                FragmentManager fragmentManager = getFragmentManager();
                expirationTimeFragment.show(Objects.requireNonNull(fragmentManager), "TimeDialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (requestCode == pick_image) {
            if (resultCode == RESULT_OK) {
                try {
                    mImageUri = imageReturnedIntent.getData();
                    final InputStream imageStream = getContext().getContentResolver().openInputStream(mImageUri);
                    selectedImage = BitmapFactory.decodeStream(imageStream);
                    String image64 = ImageUtil.convert(selectedImage);
                    UploadParameters.Builder builder = new UploadParameters.Builder();
                    params = builder.imageBase64(image64).expirationTime(ExpirationTime.fromLong(expTimeValue)).build();
                    new UploaderTask().execute();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class UploaderTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            OptionalResponse resp = ImgUploader.upload(params);
            Img img = new Img(resp.get().getResponseData().getViewerUrl(), mImageUri);
            ImgLab.get(getActivity()).persistImage(selectedImage, img);
            img.setPhotoFile(ImgLab.imgFile);
            ImgLab.get(getActivity()).addImg(img);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateUI();
            button.setEnabled(true);
        }
    }

    private class ImgHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mDateTextView;
        private final TextView mUrlTextView;
        private final ImageView mImageItem;
        private final TextView mTimer;
        private Img mImg;

        public ImgHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_img, parent, false));
            itemView.setOnClickListener(this);
            mDateTextView = itemView.findViewById(R.id.date_img);
            mUrlTextView = itemView.findViewById(R.id.url_img);
            mImageItem = itemView.findViewById(R.id.img_item);
            mTimer = itemView.findViewById(R.id.count_down_timer);

            Button copyUrlButton = itemView.findViewById(R.id.button_copy_url);
            copyUrlButton.setOnClickListener(view -> {
                ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", mUrlTextView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), R.string.link_copied, Toast.LENGTH_SHORT).show();
            });

            Button deleteItemButton = itemView.findViewById(R.id.button_delete_img);
            deleteItemButton.setOnClickListener(view -> {
                ImgLab.get(getActivity()).deleteImg(mImg);
                updateUI();
            });

        }


        public void bind(Img img) {
            mImg = img;
            mDateTextView.setText(mImg.getDate());
            mUrlTextView.setText(mImg.getUrl());
            mImageItem.setImageBitmap(ImageUtil.getScaledBitmap(mImg.getPhotoFile().toString(), Objects.requireNonNull(getActivity())));

            new CountDownTimer(img.getTimer() - System.currentTimeMillis(), 1000) {
                public void onTick(long millisUntilFinished) {
                    mTimer.setText(getString(R.string.time_left) + ImageUtil.showTimer(millisUntilFinished / 1000));
                }

                public void onFinish() {
                    mTimer.setText("Срок жизни изображения истек");
                    ImgLab.get(getActivity()).deleteImg(img);
                    try {
                        ImgbbUpFragment.this.wait(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateUI();
                }
            }.start();
        }

        @Override
        public void onClick(View view) {
            Intent openLink = new Intent(Intent.ACTION_VIEW, Uri.parse(mImg.getUrl()));
            startActivity(openLink);
            Toast.makeText(getActivity(),
                    mImg.getDate() + " clicked!", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public class ImgAdapter extends RecyclerView.Adapter<ImgHolder> {
        private List<Img> mImgs;

        public ImgAdapter(List<Img> imgs) {
            mImgs = imgs;
        }

        @NonNull
        @Override
        public ImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ImgHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ImgHolder holder, int position) {
            Img img = mImgs.get(position);
            holder.bind(img);

        }

        @Override
        public int getItemCount() {
            return mImgs.size();
        }

        public void setImgs(List<Img> imgs) {
            mImgs = imgs;
        }
    }
}






