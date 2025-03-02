import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moodsync.R;

public class AddMoodFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button createMoodButton = view.findViewById(R.id.createmood);

        createMoodButton.setOnClickListener(v -> showSuccessDialog());
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Upload Success")
                .setMessage("Your mood has been successfully uploaded.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
