package demo;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

/**
 * Created by hirokinaganuma on 2016/10/13.
 */
public class Sample02 {
    public static void main(String[] args) throws Exception {
        String master;
        if (args.length > 0) {
            master = args[0];
        } else {
            master = "local[*]";
        }
        JavaSparkContext sc = new JavaSparkContext(master, "basicavg", System.getenv("SPARK_HOME"), System.getenv("JARS"));

        JavaRDD<String> imsiList = sc.textFile("bin/input/Dummydata/imsi-list.txt");//imsiとオペレーターIDの関連付けのリスト
        JavaRDD<String> operatorList = sc.textFile("bin/input/Dummydata/operator-list.txt");//オペレーターID一覧のリスト
        JavaRDD<String> imsiItemList = sc.textFile("bin/input/Dummydata/imsi-item-list.txt");//imsiとそれに紐づく課金（ITEM-1からITEM-3とそれぞれの費用）が入っている

        JavaPairRDD<String, Object> imsiPairRDD = imsiList.mapToPair(s-> new Tuple2(s.split(",")[0],s.split(",")[1]));//imsiPairRDDはオペレータ−IDがkey,imsi番号がvalue

        System.out.println(imsiPairRDD.partitions());
        JavaPairRDD<String,Integer> itemPairRDD = imsiItemList.mapToPair(s-> new Tuple2(s.split(",")[0],Integer.valueOf(s.split(",")[2])));//imsiPairRDDはオペレータ−IDがkey,課金額がvalue

        JavaPairRDD<String,Integer> itemReducedPairRDD = itemPairRDD.reduceByKey((x, y) -> x + y);//imsiごとの集計結果のLIST,imsiがkey,合計か金額がvalue

        JavaRDD preJoinPairRDD = imsiPairRDD.join(itemReducedPairRDD).values();
        JavaPairRDD<String, Integer> joinPairRDD = JavaPairRDD.fromJavaRDD(preJoinPairRDD);
        JavaPairRDD<String, Integer> resultRDD = joinPairRDD.reduceByKey((x, y) -> x + y).sortByKey();

        resultRDD.saveAsTextFile("bin/output/resultRDD");
        ////最終出力としては、オペレーターIDごとに、もっているIMSIのITEM1から3までの合計が出る形
    }
}
