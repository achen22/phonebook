package com.example.phonebook.data;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    // Binary search
    public static int searchContactsById(List<Contact> sortedList, long id) {
        if (sortedList.isEmpty()) {
            // id does not exist in empty list
            return -1;
        }

        // index of id should always be between low and high
        int low = 0;
        int high = sortedList.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            long currentId = sortedList.get(mid).getId();
            if (currentId == id) {
                // found it!
                return mid;
            } else if (currentId < id) {
                // everything at and before the current position is too low
                low = mid + 1;
            } else {
                // everything at and after the current position is too high
                high = mid - 1;
            }
        }

        // didn't find it
        return -1-low;
    }

    // Natural merge sort, returns index of first item that starts with a letter
    public static int sortContactsByName(List<Contact> list, boolean reverse) {
        // Trivial cases where list is already sorted
        if (list.isEmpty()) {
            return 0;
        }  else if (list.size() <= 1) {
            Contact contact = list.get(0);
            char c = Character.toUpperCase(contact.getName().charAt(0));
            return c >= 'A' && c <= 'Z' ? 1 : 0;
        }

        List<List<Contact>> sortList = new ArrayList<>();
        List<Contact> currentList = new ArrayList<>();
        sortList.add(currentList);
        int parity = reverse ? -1 : 1;

        // Split original list into smaller sorted lists
        String prev = list.get(0).getName();
        currentList.add(list.remove(0));
        while (!list.isEmpty()) {
            // Create new list if adding the new item makes the list unsorted
            int comparison = prev.compareToIgnoreCase(list.get(0).getName());
            if (comparison * parity > 0) {
                currentList = new ArrayList<>();
                sortList.add(currentList);
            }
            prev = list.get(0).getName();
            currentList.add(list.remove(0));
        }

        // Merge smaller lists until only one remains
        while (sortList.size() != 1) {
            int index = 0;
            while (sortList.size() > index + 1) {
                List<Contact> list1 = sortList.remove(index);
                List<Contact> list2 = sortList.remove(index);
                List<Contact> sorted = new ArrayList<>();
                sortList.add(index, sorted);

                while (!list1.isEmpty() && !list2.isEmpty()) {
                    // Since both lists are sorted, we only need to compare the first element of the two lists
                    int comparison = list1.get(0).getName()
                            .compareToIgnoreCase(list2.get(0).getName());
                    if (comparison * parity <= 0) {
                        sorted.add(list1.remove(0));
                    } else {
                        sorted.add(list2.remove(0));
                    }
                }

                // One of the lists is empty now, so just add the contents of
                // the other to the end of the sorted list
                sorted.addAll(list1);
                sorted.addAll(list2);
                index++;
            }
        }

        list.addAll(sortList.get(0));

        // Move non-alphabet characters to start
        int pos; // Position to insert non-alphabet characters
        int size = list.size();
        if (!reverse) {
            // Find index to insert from
            for (pos = 0; pos < size; pos++) {
                Contact contact = list.get(pos);
                char c = contact.getName().charAt(0);
                if (Character.toUpperCase(c) >= 'A') {
                    break;
                }
            }
            // Move back items until we reach a letter
            for (int i = 0; i < size - pos; i++) {
                Contact contact = list.remove(size - 1);
                char c = contact.getName().charAt(0);
                if (Character.toUpperCase(c) <= 'Z') {
                    list.add(contact);
                    return pos + i;
                }
                list.add(pos, contact);
            }
        } else { // reverse
            for (pos = 0; pos < size; pos++) {
                Contact contact = list.get(pos);
                char c = contact.getName().charAt(0);
                if (Character.toUpperCase(c) <= 'Z') {
                    break;
                }
            }
            // Move back items until we reach a letter
            for (int i = 0; i < size - pos; i++) {
                Contact contact = list.remove(size - 1);
                char c = contact.getName().charAt(0);
                if (Character.toUpperCase(c) >= 'A') {
                    list.add(contact);
                    return pos + i;
                }
                list.add(pos, contact);
            }
        }

        // all items are non-alphabet characters
        return size;
    }

    public static int hash(String s) {
        return s == null || s.isEmpty() ? 0 : s.charAt(0);
    }

    public static int[] search(List<String> list, String s) {
        int hash = hash(s);
        int offset = 0;
        assert list.size() > 0;

        while (!list.isEmpty() && hash(list.get(0)) < hash) {
            offset++;
            list.remove(0);
        }

        if (list.isEmpty() || hash(list.get(0)) != hash) {
            return new int[] { offset, offset };
        }

        int start = 1;
        while (start != list.size() && hash(list.get(start)) == hash) {
            start++;
        }
        list.subList(start, list.size()).clear();

        int length = list.size();
        if (s.length() == 1) {
            return new int[] { offset, offset + length };
        }
        for (int i = 0; i < length; i++) {
            list.set(i, list.get(i).substring(1));
        }

        int[] result = search(list, s.substring(1));
        result[0] += offset;
        result[1] += offset;
        return result;
    }
}
