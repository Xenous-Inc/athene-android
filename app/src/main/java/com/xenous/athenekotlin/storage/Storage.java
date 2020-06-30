package com.xenous.athenekotlin.storage;

import com.xenous.athenekotlin.data.Category;
import com.xenous.athenekotlin.data.Word;

import java.util.ArrayList;

public class Storage {

    private static Storage instance;

    private static ArrayList<Word> wordArrayList = new ArrayList<>();
    private static ArrayList<Word> checkingWordsArrayList = new ArrayList<>();

    private static ArrayList<Category> categoryArrayList = new ArrayList<>();

    public static Storage getInstance() {
        if(instance == null) {
            instance = new Storage();
        }

        return instance;
    }

    public ArrayList<Word> getWordArrayList() {
        return wordArrayList;
    }

    public void setWordArrayList(ArrayList<Word> wordArrayList) {
        Storage.wordArrayList = wordArrayList;
    }

    public ArrayList<Word> getCheckingWordsArrayList() {
        return checkingWordsArrayList;
    }

    public void setCheckingWordsArrayList(ArrayList<Word> checkingWordsArrayList) {
        Storage.checkingWordsArrayList = checkingWordsArrayList;
    }

    public ArrayList<Category> getCategoryArrayList() {
        return categoryArrayList;
    }

    public void setCategoryArrayList(ArrayList<Category> categoryArrayList) {
        Storage.categoryArrayList = categoryArrayList;
    }
}
