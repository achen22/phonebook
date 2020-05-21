package com.example.phonebook.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContactsHashTable {
    private List<Contact> contacts;
    private char[] sections;
    private int[] index;

    public ContactsHashTable(List<Contact> contacts, boolean reverse) {
        this.contacts = new ArrayList<>(contacts);
        int pos = Utils.sortContactsByName(this.contacts, reverse);

        // create sections
        int length = 26;
        char WILDCARD = '#';
        sections = new char[length + 1];
        sections[0] = WILDCARD;
        for (int i = 0; i < length; i++) {
            int position = reverse ? length - 1 - i : i;
            sections[position + 1] = (char) ('A' + i);
        }

        // get indexes
        index = new int[sections.length];
        index[0] = 0;
        index[1] = pos;
        int i;
        for (i = 2; i < index.length && pos != this.contacts.size(); i++) {
            while (hash(this.contacts.get(pos)) > sections[i] == reverse) {
                if (++pos == this.contacts.size()) {
                    break;
                }
            }
            index[i] = pos;
        }
        Arrays.fill(index, i, index.length, pos);
    }

    public static char hash(Contact contact) {
        return Character.toUpperCase(contact.getName().charAt(0));
    }

    public int[] getIndex() {
        return index.clone();
    }

    public char[] getSections() {
        return sections.clone();
    }

    public List<Contact> toList() {
        return new ArrayList<>(contacts);
    }
}
