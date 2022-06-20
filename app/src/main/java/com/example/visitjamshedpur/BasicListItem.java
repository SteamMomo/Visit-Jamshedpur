package com.example.visitjamshedpur;


public class BasicListItem {
    private final String mImage1;
    private final String mTitle;
    private final String mAddress;
    private final String mId;

    public BasicListItem(String image1, String title, String address, String Id) {
        mImage1 = image1;
        mTitle = title;
        mAddress = address;
        mId = Id;
    }

    public String getmId() { return mId; }

    public String getImage1() {
        return mImage1;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAddress() {
        return mAddress;
    }
}
