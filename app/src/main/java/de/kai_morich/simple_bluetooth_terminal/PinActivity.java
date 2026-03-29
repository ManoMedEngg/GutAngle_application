package de.kai_morich.simple_bluetooth_terminal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PinActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_PIN = "app_pin";

    private StringBuilder currentPin = new StringBuilder();
    private String firstPin = "";
    private boolean isSettingPin = false;
    private boolean isConfirmingPin = false;

    private View[] dots;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        tvTitle = findViewById(R.id.pin_title);
        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3),
                findViewById(R.id.dot4),
                findViewById(R.id.dot5),
                findViewById(R.id.dot6)
        };

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedPin = prefs.getString(KEY_PIN, null);

        if (savedPin == null) {
            isSettingPin = true;
            tvTitle.setText("Set 6-Digit PIN");
        } else {
            tvTitle.setText("Enter PIN");
        }

        setupKeyboard();
    }

    private void setupKeyboard() {
        int[] btnIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int id : btnIds) {
            findViewById(id).setOnClickListener(v -> {
                if (currentPin.length() < 6) {
                    currentPin.append(((Button) v).getText());
                    updateDots();
                    if (currentPin.length() == 6) {
                        handlePinEntry();
                    }
                }
            });
        }

        findViewById(R.id.btn_del).setOnClickListener(v -> {
            if (currentPin.length() > 0) {
                currentPin.deleteCharAt(currentPin.length() - 1);
                updateDots();
            }
        });

        findViewById(R.id.btn_clear).setOnClickListener(v -> {
            currentPin.setLength(0);
            updateDots();
        });
    }

    private void updateDots() {
        for (int i = 0; i < dots.length; i++) {
            if (i < currentPin.length()) {
                dots[i].setBackgroundResource(R.drawable.pin_dot_on);
            } else {
                dots[i].setBackgroundResource(R.drawable.pin_dot_off);
            }
        }
    }

    private void handlePinEntry() {
        String enteredPin = currentPin.toString();

        if (isSettingPin) {
            if (!isConfirmingPin) {
                firstPin = enteredPin;
                isConfirmingPin = true;
                currentPin.setLength(0);
                updateDots();
                tvTitle.setText("Confirm PIN");
                Toast.makeText(this, "Enter PIN again to confirm", Toast.LENGTH_SHORT).show();
            } else {
                if (enteredPin.equals(firstPin)) {
                    savePin(enteredPin);
                    startMain();
                } else {
                    Toast.makeText(this, "PINs do not match. Try again.", Toast.LENGTH_SHORT).show();
                    resetToSetPin();
                }
            }
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String savedPin = prefs.getString(KEY_PIN, "");
            if (enteredPin.equals(savedPin)) {
                startMain();
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                currentPin.setLength(0);
                updateDots();
            }
        }
    }

    private void resetToSetPin() {
        isSettingPin = true;
        isConfirmingPin = false;
        firstPin = "";
        currentPin.setLength(0);
        updateDots();
        tvTitle.setText("Set 6-Digit PIN");
    }

    private void savePin(String pin) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_PIN, pin)
                .apply();
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
