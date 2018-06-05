package com.generator;

import com.squareup.okhttp.*;
import org.fluttercode.datafactory.impl.DataFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Main {
    private static List<String> list = new ArrayList<>();
    private static final int NUMBER_OF_USERS = 1000;

    private static OkHttpClient client = new OkHttpClient();
    private static long start;
    private static DataFactory dataFactory = new DataFactory();

    public static void main(String[] args) throws InterruptedException {
        start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(50);

        IntStream.range(0, NUMBER_OF_USERS).forEach(index -> executorService.submit(getIntConsumer()));
//        System.out.println("Number of active threads from the given thread 2: " + Thread.activeCount());

        while (list.size() != NUMBER_OF_USERS) {
            Thread.sleep(500);
        }

        IntStream.range(0, NUMBER_OF_USERS)
                .forEach(index -> executorService.submit(() -> new RelationshipsGenerator(list, start).generateRelations(index)));
    }

    private static Runnable getIntConsumer() {
        return () -> {
            String name = dataFactory.getFirstName() + "_" + dataFactory.getLastName();
            String city = dataFactory.getCity().replace(" ", "_");
            Date date = dataFactory.getDateBetween(new Date(-System.currentTimeMillis()), new Date());
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

            System.out.println(name + "-" + city + "-" + formattedDate);

            Request request = new Request.Builder()
                    .header("name", name).header("city", city).header("birthDate", formattedDate)
                    .url("http://localhost:8085/register")
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Response response) {
                    String mongoId = response.header("mongoId");
                    System.out.println(mongoId);
                    list.add(mongoId);
                }
            });
        };
    }
}
