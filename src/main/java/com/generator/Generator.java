package com.generator;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
    private static final int NUMBER_OF_USERS = 100;
    private static final int PERCENT_OF_FRIENDS = 10;
    private static final int PERCENT_OF_INVITATIONS = 10;

    private static Random generator = new Random();
    private static HttpClient client = HttpClients.createDefault();

    public static void main(String[] args) throws IOException {
        DataFactory dataFactory = new DataFactory();
        for (int i = 0; i < NUMBER_OF_USERS; i++) {
            String name = dataFactory.getFirstName() + "_" + dataFactory.getLastName();
            String city = dataFactory.getCity().replace(" ", "_");
            Date date = dataFactory.getDateBetween(new Date(-System.currentTimeMillis()), new Date());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            System.out.println(name + "-" + city + "-" + dateFormat.format(date));

            String headers = "?name=" + name + "&city=" + city + "&birthDate=" + dateFormat.format(date);
            HttpPost register = new HttpPost("http://localhost:8085/register" + headers);
            String mongoId = client.execute(register).getFirstHeader("mongoId").getElements()[0].toString();
            list.add(mongoId);
//            System.out.println(mongoId);
        }

        IntStream.range(0, list.size()).forEach(Generator::generateRelations);
    }

    private static void generateRelations(int index) {
        generateFriends(index);
//            generateInvitations(index);
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
        client.execute(createInvitationPost(index, generator.nextInt(NUMBER_OF_USERS)));
    }

    private static void generateFriends(int index) {
        System.out.println(NUMBER_OF_USERS * PERCENT_OF_FRIENDS / 100);
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
        client.execute(createInvitationPost(index, friendIndex));
        client.execute(createInvitationPost(friendIndex, index));
    }

    private static HttpPost createInvitationPost(int inviterIndex, int inviteeIndex) {
        HttpPost httpPost = new HttpPost("http://localhost:8085/" + list.get(inviteeIndex) + "/invite?inviteeId=" + list.get(inviterIndex));
        System.out.println(httpPost);
        return httpPost;
    }
}
