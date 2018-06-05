package com.generator;

import com.squareup.okhttp.*;
import org.fluttercode.datafactory.impl.DataFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class Generator {
    private static List<String> list = new ArrayList<>();
    private static final int NUMBER_OF_USERS = 100  ;
    private static final int PERCENT_OF_FRIENDS = 10;
    private static final int PERCENT_OF_INVITATIONS = 3;

    private static Random generator = new Random();
    private static OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws IOException {
        DataFactory dataFactory = new DataFactory();
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_OF_USERS; i++) {
            String name = dataFactory.getFirstName() + "_" + dataFactory.getLastName();
            String city = dataFactory.getCity().replace(" ", "_");
            Date date = dataFactory.getDateBetween(new Date(-System.currentTimeMillis()), new Date());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            System.out.println(name + "-" + city + "-" + dateFormat.format(date));

            Request request = new Request.Builder()
                    .header("name", name).header("city", city).header("birthDate", dateFormat.format(date))
                    .url("http://localhost:8085/register")
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""))
                    .build();
            Response response = client.newCall(request).execute();
            String mongoId = response.header("mongoId");
            System.out.println(mongoId);
            list.add(mongoId);
        }

        IntStream.range(0, list.size()).forEach(Generator::generateRelations);
        long stop = System.currentTimeMillis();
        System.out.println(stop - start);
    }

    private static void generateRelations(int index) {
        generateFriends(index);
        generateInvitations(index);
    }

    private static void generateInvitations(int index) {
        IntStream.of(NUMBER_OF_USERS * PERCENT_OF_INVITATIONS / 100).forEach(e -> {
            try {
                generateInvitation(index);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }

    private static void generateInvitation(int index) throws IOException {
        createInvitationPost(index, generator.nextInt(NUMBER_OF_USERS));
    }

    private static void generateFriends(int index) {
        System.out.println("index: " + index);
        IntStream.range(0, NUMBER_OF_USERS * PERCENT_OF_FRIENDS / 100).forEach(e ->
        {
            try {
                generateFriend(index);
                System.out.println(e);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }

    private static void generateFriend(int index) throws IOException {
        int friendIndex = generator.nextInt(NUMBER_OF_USERS);
        createInvitationPost(index, friendIndex);
        createInvitationPost(friendIndex, index);
    }

    private static void createInvitationPost(int inviterIndex, int inviteeIndex) throws IOException {
        Request request = new Request.Builder()
                .header("inviteeId", list.get(inviterIndex))
                .url("http://localhost:8085/" + list.get(inviteeIndex) + "/invite")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""))
                .build();
        client.newCall(request).execute();
    }
}
