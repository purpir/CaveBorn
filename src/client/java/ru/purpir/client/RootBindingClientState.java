package ru.purpir.client;

import java.util.ArrayList;
import java.util.List;

public final class RootBindingClientState {
    private static volatile List<Link> links = List.of();

    private RootBindingClientState() {
    }

    public static void setLinks(List<Integer> entityLinks) {
        List<Link> nextLinks = new ArrayList<>();
        for (int i = 0; i + 1 < entityLinks.size(); i += 2) {
            nextLinks.add(new Link(entityLinks.get(i), entityLinks.get(i + 1)));
        }
        links = List.copyOf(nextLinks);
    }

    public static void clear() {
        links = List.of();
    }

    public static List<Link> getLinks() {
        return links;
    }

    public record Link(int fromEntityId, int toEntityId) {
    }
}
