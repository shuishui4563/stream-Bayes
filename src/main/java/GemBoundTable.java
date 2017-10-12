import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by storm on 21/07/17.
 */
public class GemBoundTable {
    public double[] p_hat = new double[1000];
    public int N;
    public double[] R = new double[1000000];
    public double[][] BT = new double[1000][2];
    public ArrayList<Integer> I = new ArrayList<Integer>();
    public static int M;
    public double yita;
    public double sigma;
    public double delta;
    GemBoundTable() {
//        this.yita = 0.9;
//        this.N = 100;
//        this.M = 1000000;
////        this.M = 1000;
//        this.sigma = 0.000001;
//        this.delta = 0.01;
////        this.sigma = 0.01;
////        this.delta = 0.1;
//        for(int i=0;i<1000;i++){
//            this.p_hat[i] = 0.001*(i+1);
//        }
//
//        File writename = new File("/home/sue/GemBoundMat.txt");
//        try {
//            writename.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//        BufferedWriter out = null;
//        try {
//            out = new BufferedWriter(new FileWriter(writename));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        double p = 0.50;
//
//        for(int i=0;i<1000;i++){
//            this.R = this.getRBound(this.p_hat[i]);
//            Arrays.sort(this.R);
//            BT[i][0] = this.percentile(this.R,1-this.sigma);
//            BT[i][1] = this.percentile(this.R,1-this.delta);
//            try {
//                out.write(BT[i][0] + "\t" + BT[i][1] +"\n");
//                out.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        try
        {
//            String encoding = "GBK";
            File file = new File("/home/sue/GemBoundMat.txt");
            if (file.isFile() && file.exists())
            { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                String[] result = new String[2];

                for(int i=0;i<1000;i++){
                    lineTxt = bufferedReader.readLine();
                    if(lineTxt!=""){
                        result= lineTxt.split("\t");
                        this.BT[i][0] = Double.parseDouble(result[0]);
                        this.BT[i][1] = Double.parseDouble(result[1]);

                    }
                }
                bufferedReader.close();
                read.close();
            }
            else
            {
                System.out.println("找不到指定的文件");
            }
        }
        catch (Exception e)
        {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }


    }

    public double[] getBound(double p_rate){
        int index = (int)java.lang.Math.floor(p_rate/0.001);
//        System.out.println(index);
        double[] result = new double[2];
        result[0] = this.BT[index-1][0];
        result[1] = this.BT[index-1][1];
        return result;
    }
    public  double[] getRBound(double p)
    {
        bernoulli ber = new bernoulli();
        double[] R = new double[M]; //检验量
        for(int j=0;j<M;j++)
        {
            this.I = (ArrayList<Integer>) ber.Bernouli(N,p);
            R[j] = getresult(I);

        }
        return R;
    }

    public   Double getresult(ArrayList<Integer> I)
    {
        Double result = 0.0;
        Double re_pow = 0.0;
        Double re = 0.0;
        for(int i=0;i<N;i++)
        {
            if(I.get(i)!=0){
                re_pow = Math.pow(yita,(N-i-1));
                re  = re_pow * I.get(i);
            }
            result += re;
        }
        result = result*(1-yita);
        return result;
    }

    public double percentile(double[] data,double p){
        int n = data.length;

        double px =  p*(n-1);
        int i = (int)java.lang.Math.floor(px);
        double g = px - i;
        if(g==0){
            return data[i];
        }else{
            return (1-g)*data[i]+g*data[i+1];
        }
    }

    public static void main(String[] args){
        GemBoundTable gemBoundTable = null;
        gemBoundTable = new GemBoundTable();

//        double[] result = gemBoundTable.getBound(0.05);
//        System.out.println(result[1]+" "+result[0]);
    }
}
