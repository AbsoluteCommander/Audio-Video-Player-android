package is.xyz.mpv;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;

import is.xyz.filepicker.DocumentPickerFragment;

public class MPVDocumentPickerFragment extends DocumentPickerFragment {

    public MPVDocumentPickerFragment(@NonNull Uri root) {
        super(root);
    }

    @Override
    public void onClickCheckable(@NonNull View view, @NonNull FileViewHolder vh) {
        Intent i = new Intent(Intent.ACTION_VIEW, vh.file, getActivity(), MPVActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        getActivity().finish();
    }
}
