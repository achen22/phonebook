package com.example.phonebook.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ContactsHashTable {
    private List<Contact> contacts;
    private String[] sections;
    private int[] index;
    private List<Contact>[] hashTable;
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

        // get indexes, create hash table
        hashTable = (List<Contact>[]) new ArrayList<?>[sections.length];
        for (int i = 0; i < sections.length; i++) {
            hashTable[i] = new ArrayList<>();
        }
        index = new int[sections.length];

        index[0] = 0;
        for (int i = 0; i < pos; i++) {
            hashTable[0].add(this.contacts.get(i));
        }

        if (pos == this.contacts.size()) {
            return;
        }

        int i;
        for (i = 1; i < index.length && pos != this.contacts.size(); i++) {
            index[i] = pos;
            while (hashName(this.contacts.get(pos).getName()) == i) {
                hashTable[i].add(this.contacts.get(pos));
                if (++pos == this.contacts.size()) {
                    break;
                }
            }
        }
        if (i != index.length) {
            index[i] = pos;
            Arrays.fill(index, i + 1, index.length, pos);
        }
    }

    private int hashName(String name) {
        char c = Character.toUpperCase(name.charAt(0));
        int index = c >= 'A' && c <= 'Z'
                ? c - 'A' + 1
                : 0;
        return (reverse && index != 0)
                ? sections.length - index
                : index;
    }

    public int getSectionForIndex(int index) {
        return index >= 0 && index < contacts.size()
                ? hashName(contacts.get(index).getName())
                : -1;
    }

    public int[] getIndex() {
        return index.clone();
    }

    public String[] getSections() {
        return sections.clone();
    }

    public List<Contact> searchName(String name) {
        if (name == null || name.isEmpty()) {
            return this.toList();
        }

        int hash = hashName(name);
        List<Contact> contacts = hashTable[hash];
        if (contacts.isEmpty() || name.length() == 1 && hash != 0) {
            return contacts;
        }

        List<String> names = new ArrayList<>();
        if (hash == 0) {
            for (Contact contact : contacts) {
                names.add(contact.getName().toLowerCase());
            }
        } else {
            name = name.substring(1);
            for (Contact contact : contacts) {
                names.add(contact.getName().substring(1).toLowerCase());
            }
        }
        if (reverse) {
            Collections.reverse(names);
        }

        int[] result = Utils.search(names, name.toLowerCase());
        if (reverse) {
            int size = contacts.size();
            return new ArrayList<>(contacts.subList(
                    size - result[1],
                    size - result[0]
            ));
        }
        return new ArrayList<>(contacts.subList(result[0], result[1]));
    }

    public List<Contact> toList() {
        return new ArrayList<>(contacts);
    }

    public boolean isSectionEmpty(int section) {
        return hashTable[section].isEmpty();
    }

    public boolean isEmpty() {
        return contacts.isEmpty();
    }
}
