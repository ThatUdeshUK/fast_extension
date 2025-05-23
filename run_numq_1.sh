#!/usr/local/bin/bash

java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.RunFAST /scratch1/ukumaras/projects/fast/out/ /homes/ukumaras/scratch/twitter-data/tweets_o2000000_q20000000_scaled_max_keys3.json 0.2 5 1000000 100000 _100ko_numq_tweets
java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.RunFAST /scratch1/ukumaras/projects/fast/out/ /homes/ukumaras/scratch/twitter-data/tweets_o2000000_q20000000_scaled_max_keys3.json 0.2 5 5000000 100000 _100ko_numq_tweets
java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.RunFAST /scratch1/ukumaras/projects/fast/out/ /homes/ukumaras/scratch/twitter-data/tweets_o2000000_q20000000_scaled_max_keys3.json 0.2 5 10000000 100000 _100ko_numq_tweets
java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.RunFAST /scratch1/ukumaras/projects/fast/out/ /homes/ukumaras/scratch/twitter-data/tweets_o2000000_q20000000_scaled_max_keys3.json 0.2 5 15000000 100000 _100ko_numq_tweets
java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.RunFAST /scratch1/ukumaras/projects/fast/out/ /homes/ukumaras/scratch/twitter-data/tweets_o2000000_q20000000_scaled_max_keys3.json 0.2 5 20000000 100000 _100ko_numq_tweets

java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.RunCkQST /scratch1/ukumaras/projects/fast/output_omega/ ~/scratch/twitter-data/tweets_o2000000_q20000000_scaled_max_keys3.json 5 1000000 1000000 _100ko_numq_tweets
java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.RunCkQST /scratch1/ukumaras/projects/fast/output_omega/ ~/scratch/twitter-data/tweets_o2000000_q20000000_scaled_max_keys3.json 5 5000000 1000000 _100ko_numq_tweets
java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.RunCkQST /scratch1/ukumaras/projects/fast/output_omega/ ~/scratch/twitter-data/tweets_o2000000_q20000000_scaled_max_keys3.json 5 10000000 1000000 _100ko_numq_tweets
java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.RunCkQST /scratch1/ukumaras/projects/fast/output_omega/ ~/scratch/twitter-data/tweets_o2000000_q20000000_scaled_max_keys3.json 5 15000000 1000000 _100ko_numq_tweets
java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.RunCkQST /scratch1/ukumaras/projects/fast/output_omega/ ~/scratch/twitter-data/tweets_o2000000_q20000000_scaled_max_keys3.json 5 20000000 1000000 _100ko_numq_tweets
