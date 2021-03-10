package ru.dopegeek.imgbbupldr;

import androidx.fragment.app.Fragment;

public class ImgbbUpActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ImgbbUpFragment();
    }


}