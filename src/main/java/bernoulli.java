/**
 * Created by storm on 16/07/17.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class bernoulli {

    //随机数对象
    private static Random random;
    //用于产生随机数的种子
    private static long seed;

    // 静态初始化区域
    static {
        //产生随机数种子
        seed = System.currentTimeMillis();
        random = new Random(seed);
    }
    /**
     * 返回一个随机的范围在[0,1)之间的double类型的数
     */
    public static double uniform() {
//        System.out.println(random.nextDouble());
        return random.nextDouble();
    }


    public static boolean bernoulli(double p) {
        double result = uniform();
//        System.out.println(result);
        return result <= p;
    }

    public List<Integer> Bernouli(int N, double p){
        ArrayList<Integer> result = new ArrayList<Integer>();
        for(Integer i = 0;i<N;i++)
        {
            if(bernoulli(p)){
                result.add(0);
            }
            else{
                result.add(1);
            }
        }
        return result;
    }

    public static void main(String[] args){
        for(int i=0;i<10;i++){
            if(bernoulli(0.5)){
                System.out.println(0);
            }
            else{
                System.out.println(1);
            }
        }
    }
}
