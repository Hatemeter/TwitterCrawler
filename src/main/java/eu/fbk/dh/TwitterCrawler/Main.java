package eu.fbk.dh.TwitterCrawler;

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        System.setProperty("twitter4j.http.httpClient","twitter4j.WorkaroundLongTweetHttpClientImpl");

        Options options = new Options();


        Option o = new Option("h", "help", false, "help message");
        options.addOption(o);
        options.addOption("r", "retweet", false, "include retweet in the extraction");


        Option tags = Option.builder("t")
                .argName("tags1,tag2,...,tag_n")
                .required(true)
                .hasArgs()
                .valueSeparator(',')
                .longOpt("tags")
                .desc("List of hashtag comma separated (please add/leave '#' to the top of hashtag)")
                .build();

        options.addOption(tags);

        Option sinceid_opt = Option.builder("s")
                .argName("id1,id2,...,id_n")
                .hasArgs()
                .valueSeparator(',')
                .longOpt("since")
                .desc("List of id comma separated (same number of tags)")
                .build();

        options.addOption(sinceid_opt);


        HelpFormatter formatter = new HelpFormatter();


        int cores = Runtime.getRuntime().availableProcessors();
        if (cores > 4) {
            cores = cores / 2;
        }



        formatter.setWidth(800);
        CommandLine cmd = null;
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        } catch (Exception e) {
            formatter.printHelp("TwitterCrawler", options);
            System.exit(0);
        }


        String additional_query_params = " -filter:retweets";


        if (cmd.hasOption('r')) {
            additional_query_params = "";
        }






        String[] comma_sep_tags = cmd.getOptionValues("t");
        List<String> hashtags = Arrays.asList(comma_sep_tags);
        List<String> since_id = new ArrayList<>();






        if (cmd.hasOption('s')) {
            String[] id_sep_tags = cmd.getOptionValues("s");
            since_id = Arrays.asList(id_sep_tags);

            if (since_id.size() != hashtags.size()) {
                formatter.printHelp("TwitterCrawler", options);
                System.exit(0);
            }


        }


        ExecutorService executor = Executors.newFixedThreadPool(cores);
        for (int i = 0; i < hashtags.size(); i++) {

            Long since = null;

            if (since_id.size() > 0) {
                try {
                    since = Long.parseLong(since_id.get(i).trim());
                } catch (Exception e) {
                    System.out.println("Un-parsable since id ...");
                    formatter.printHelp("TwitterCrawler", options);
                    System.exit(0);
                }
            }

            String tag = hashtags.get(i).trim();
            //System.out.println(tag);
            System.out.println("Thread initialization: " + tag + (since!=null? " since id:"+since : "") );
            ThreadCrawler t = new ThreadCrawler( tag, additional_query_params, since);
            executor.execute(t);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


//
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//
//        for(String tag : hashtags){
//
//
//            System.out.println("Thread initialization: #"+tag.replace("#","").trim());
//            ThreadCrawler t = new ThreadCrawler("#"+(tag.replace("#","").trim()),additional_query_params,);
//            executor.execute(t);
//        }
//
//        executor.shutdown();
//        try {
//            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//


    }
}
