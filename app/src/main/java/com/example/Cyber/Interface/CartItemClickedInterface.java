package com.example.Cyber.Interface;

import android.widget.TextView;

public interface CartItemClickedInterface {
    void onItemClick(int position);
    void onTextChange(int position, String text, TextView view);
}
