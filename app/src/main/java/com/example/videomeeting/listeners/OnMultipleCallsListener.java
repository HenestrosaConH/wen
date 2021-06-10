package com.example.videomeeting.listeners;

public interface OnMultipleCallsListener {

    /**
     * Interface that will allow us to know if we are going to make a group call or not
     * @param isMultipleUsersSelected true if there are multiple users selected
     */
    void onMultipleUsersAction(Boolean isMultipleUsersSelected);
}
