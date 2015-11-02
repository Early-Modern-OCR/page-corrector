# page-corrector
Scala code to correct Tesseract OCR output and generate ALTO XML and text files. Uses dictionary files, rules and a google-3gram DB to make corrections.

## Prerequisites
* dictionary files
  * one or more dictionary files encoded as one-word-per-line; examples exist in `data/dictionaries/`
* rules file
  * JSON-formatted confusion rules file; an example exists in `data/rules/transformations.json`; 
  * the key represents the correct sequence, while the value(s) represent the ways in which Tesseract may erroneously recognize the correct sequence
    * example: `"B": [ "3", "H" ]` means that in the Tesseract output, a `3` or an `H` could be OCR errors of the letter `B`.
* 3-gram database
  * a 3-gram database that is used for context matching; we downloaded the publicly-available English 3-gram dataset from [Google Books Ngram Viewer](http://storage.googleapis.com/books/ngrams/books/datasetsv2.html) and loaded it into a sharded MySQL database (after prunning all 3grams containing "garbage" characters) (see below for a description of the database schema)

### Database schema
**Note**: Useful information about how to load the Google datasets into MySQL can be found here: http://dba.stackexchange.com/questions/7160/how-to-best-store-the-google-web-ngram-data

Table `ngram_key`:
```
+----------+------------------+------+-----+---------+----------------+
| Field    | Type             | Null | Key | Default | Extra          |
+----------+------------------+------+-----+---------+----------------+
| ngram_id | int(10) unsigned | NO   | UNI | NULL    | auto_increment |
| ngram    | varchar(255)     | NO   | PRI | NULL    |                |
+----------+------------------+------+-----+---------+----------------+
```
```
CREATE TABLE `ngram_key` (
  `ngram_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `ngram` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`ngram`),
  UNIQUE KEY `ngram_id_2` (`ngram_id`),
  KEY `ngram_id` (`ngram_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

Table `ngram3_lower`:
```
+-------------+-----------------------+------+-----+---------+-------+
| Field       | Type                  | Null | Key | Default | Extra |
+-------------+-----------------------+------+-----+---------+-------+
| ngram1_id   | int(10) unsigned      | NO   | PRI | NULL    |       |
| ngram2_id   | int(10) unsigned      | NO   | PRI | NULL    |       |
| ngram3_id   | int(10) unsigned      | NO   | PRI | NULL    |       |
| year        | smallint(6)           | NO   | PRI | NULL    |       |
| match_count | int(10) unsigned      | NO   | MUL | NULL    |       |
| vol_count   | mediumint(8) unsigned | NO   | MUL | NULL    |       |
+-------------+-----------------------+------+-----+---------+-------+
```
```
CREATE TABLE `ngram3_lower` (
  `ngram1_id` int(10) unsigned NOT NULL,
  `ngram2_id` int(10) unsigned NOT NULL,
  `ngram3_id` int(10) unsigned NOT NULL,
  `year` smallint(6) NOT NULL,
  `match_count` int(10) unsigned NOT NULL,
  `vol_count` mediumint(8) unsigned NOT NULL,
  PRIMARY KEY (`ngram1_id`,`ngram2_id`,`ngram3_id`,`year`),
  KEY `match_cnt_idx` (`match_count`),
  KEY `vol_cnt_idx` (`vol_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin 
```

To see what queries are run against the database for purposes of context matching, check out:
https://github.com/Early-Modern-OCR/page-corrector/blob/master/src/main/scala/edu/illinois/i3/emop/apps/pagecorrector/NgramContextMatcher.scala

## To build
`> sbt stage`

then find the resulting executable script and supporting libraries in `target/universal/stage/` folder.

## To run
`> ./target/universal/stage/bin/page-corrector --help`

```
page-corrector 1.10.0-SNAPSHOT
Early Modern OCR Project
  -a, --alt  <arg>              The number of alternatives to include in the ALTO
                                output (default = 2)
  -c, --ctx-min-match  <arg>    If specified, this value requires that context
                                matches have at least this much 'matchCount'
                                support in the context database before the match
                                is considered valid
      --ctx-min-vol  <arg>      If specified, this value requires that context
                                matches have at least this much 'volCount' support
                                in the context database before the match is
                                considered valid
      --dbconf  <arg>           Database configuration properties file
  -d, --dict  <arg>...          Specifies one or more dictionaries to use
      --dump                    Dump details of the corrections made and
                                corrections missed to individual files
  -m, --max-transforms  <arg>   The maximum number of elements in the
                                transformation 'pool' permitted per token (to seed
                                the powerset) (default = 20)
  -n, --noiseCutoff  <arg>      The noise probability cutoff value. Tokens with
                                noise probability higher than this value will be
                                removed before correction. Set to 0 to disable the
                                removal of noisy tokens. (default = 0.5)
  -o, --outputDir  <arg>        The directory where the results should be written
                                to
  -s, --save                    Save stats about which transformation rules were
                                applied
      --stats                   Print correction statistics in JSON format, at the
                                end
  -t, --transform  <arg>        The transformation rules file
      --help                    Show help message
      --version                 Show version of this program

 trailing arguments:
  page-ocr (required)   The page OCR file
```

Database configuration file format (for use with the `--dbconf` argument):
```
ctx_db_driver: com.mysql.jdbc.Driver
ctx_db_url: jdbc:mysql://<server>/<database_name>
ctx_db_user: <user>
ctx_db_pass: <password>
```
