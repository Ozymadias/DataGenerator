package com.generator;

import com.squareup.okhttp.*;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class RelationshipsGenerator {
    private Random generator = new Random();
    private OkHttpClient client = new OkHttpClient();
    private List<String> list;
    private final int NUMBER_OF_USERS;
    private long start;
    private static final int PERCENT_OF_INVITATIONS = 3;
    private static final int NUMBER_OF_FRIENDS = 10;

    public RelationshipsGenerator(List<String> list, long start) {
        this.list = list;
        this.NUMBER_OF_USERS = list.size();
        this.start = start;
    }

    public void generateRelations(int index) {
        generateFriends(index);
        generateInvitations(index);
    }

    private void generateInvitations(int index) {
        System.out.println(NUMBER_OF_USERS * PERCENT_OF_INVITATIONS / 100);
//        IntStream.of(NUMBER_OF_USERS * PERCENT_OF_INVITATIONS / 100).forEach(e -> generateInvitation(index));
    }

    private void generateInvitation(int index) {
        createInvitationPost(index, generator.nextInt(NUMBER_OF_USERS));
    }

    private void generateFriends(int index) {
        System.out.println("index: " + index);
        IntStream.range(0, NUMBER_OF_FRIENDS).forEach(e ->
        {
            generateFriend(index);
            System.out.println(e);
        });
    }

    private void generateFriend(int index) {
        int friendIndex = generator.nextInt(NUMBER_OF_USERS);
        createInvitationPost(index, friendIndex);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        createInvitationPost(friendIndex, index);
    }

    private void createInvitationPost(int inviterIndex, int inviteeIndex) {
        Request request = new Request.Builder()
                .header("inviteeId", list.get(inviteeIndex))
                .url("http://localhost:8085/" + list.get(inviterIndex) + "/invite")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) {
                System.out.println(inviterIndex + " invited " + inviteeIndex);
                System.out.println(System.currentTimeMillis() - start);
            }
        });
    }
}
