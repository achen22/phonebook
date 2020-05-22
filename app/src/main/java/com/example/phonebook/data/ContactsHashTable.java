package com.example.phonebook.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContactsHashTable {
    private List<Contact> contacts;
    private String[] sections;
    private int[] index;
    private boolean reverse;

    public ContactsHashTable(List<Contact> contacts, boolean reverse) {
        this.reverse = reverse;
        this.contacts = new ArrayList<>(contacts);
        int pos = Utils.sortContactsByName(this.contacts, reverse);

        // create sections
        int length = 26;
        String WILDCARD = "#";
        sections = new String[length + 1];
        sections[0] = WILDCARD;
        for (int i = 0; i < length; i++) {
            int position = reverse ? length - 1 - i : i;
            sections[position + 1] = String.valueOf((char) ('A' + i));
        }

        // get indexes
        index = new int[sections.length];
        index[0] = 0;
        index[1] = pos;
        int i;
        for (i = 2; i < index.length && pos != this.contacts.size(); i++) {
            while (getSectionForIndex(pos) < i) {
                if (++pos == this.contacts.size()) {
                    break;
                }
            }
            index[i] = pos;
        }
        Arrays.fill(index, i, index.length, pos);
    }

    private int hash(Contact contact) {
        char c = Character.toUpperCase(contact.getName().charAt(0));
        return c >= 'A' && c <= 'Z'
                ? c - 'A' + 1
                : 0;
    }

    public int getSectionForIndex(int index) {
        int hash = hash(contacts.get(index));
        if (hash == 0) {
            return 0;
        }
        return reverse ? 27 - hash : hash;
    }

    public int[] getIndex() {
        return index.clone();
    }

    public String[] getSections() {
        return sections.clone();
    }

    public List<Contact> toList() {
        return new ArrayList<>(contacts);
    }

    public boolean isSectionEmpty(int section) {
        if (section == sections.length - 1) {
            return index[section] == contacts.size();
        }
        return index[section] == index[section + 1];
    }
}
