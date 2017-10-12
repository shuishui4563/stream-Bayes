import moa.classifiers.core.driftdetection.AbstractChangeDetector;
import moa.core.ObjectRepository;
import moa.tasks.TaskMonitor;

/**
 * Created by storm on 14/08/17.
 */
public class LFR  extends AbstractChangeDetector{

    // 1:tpr 2:tnr 3:ppv 4:npv
//    private int[] rates = {1,2,3,4};

    private static final int M = 2000000;
    private int N = 0;
    private double[] p_rates = new double[4] ;

    private double[] r_rates = new double[4];

    //{(0,0),(0,1),(1,0),(1,1)
    private int[] confusion_mat = new int[4];

    protected double yita = 0.9;

    //显著性水平
    protected double sigma = 0.00001;
    protected  double delta = 0.01;

    private double[] warnbd = new double[4];

    private double[] detectedbd = new double[4];

    private int preisclass = -1;

    public GemBoundTable gemBoundTable = new GemBoundTable();


    protected double tpr = 0.0;

    protected  double ppv = 0.0;

    public LFR(){
        resetLearning();

    }

    public double get_precision(){
        return this.tpr;
    }

    public double get_recall(){
        return this.ppv;
    }

    public boolean getChange(){
        return this.isChangeDetected;
    }

    public boolean getWarningZone(){
        return this.isWarningZone;
    }

    public int[] getConfusion_mat()
    {
        return this.confusion_mat;
    }

    public void resetLearning(){
        for(int i=0;i<4;i++){
//            this.warnbd[i] = 0;
//            this.detectedbd[i] = 0;
            this.p_rates[i] = 0.5;
            this.r_rates[i] = 0.5;
            this.confusion_mat[i] = 1;
        }
        this.isChangeDetected = false;
        this.preisclass = -1;

    }
    /*
    返回检测的结果,输入是prediction & trueclass,但是继承类方法就是prediction,
    v=prediction.trueclass
     */
    public void input(double v) {
        if (this.isChangeDetected == true || this.isInitialized == false) {
//            System.out.println("reset"+this.isChangeDetected+" "+this.isInitialized);
            resetLearning();
            this.isInitialized = true;
            this.isChangeDetected = false;
        }
//        for(int i =0 ;i<4;i++){
//            System.out.println(this.confusion_mat[i]);
//        }


        int prediction=-1;
        int trueclass=-1;
        if(v == 0.0){
            prediction = 0;
            trueclass = 0;
            this.confusion_mat[0] = this.confusion_mat[0] + 1;
            this.preisclass = 1;
        }
        else if(v == 0.1){
            prediction = 0;
            trueclass = 1;
            this.confusion_mat[1] = this.confusion_mat[1]+1;
            this.preisclass = 0;
        }
        else if(v==1.0){
            prediction = 1;
            trueclass = 0;
            this.confusion_mat[2] = this.confusion_mat[2]+1;
            this.preisclass = 0;
        }
        else if(v == 1.1){
            prediction = 1;
            trueclass = 1;
            this.confusion_mat[3] = this.confusion_mat[3] +1;
            this.preisclass = 1;
        }
        else{
            System.out.printf("Error:bad input val:%f\n",v);
            return;
        }



        this.tpr = (double)this.confusion_mat[3]/(this.confusion_mat[2] + this.confusion_mat[3]);

        this.ppv = (double)this.confusion_mat[3]/(this.confusion_mat[1]+this.confusion_mat[3]);




        for(int i = 0; i<4;i++){
            boolean flag = isInfluenced(i,prediction,trueclass);
            if(flag){
                r_rates[i] = this.yita*r_rates[i] + (1-this.yita) * this.preisclass;
            }
            else{
                r_rates[i] = this.r_rates[i];
            }

            if(i==0 ){
                this.N = this.confusion_mat[1] + this.confusion_mat[3];
                this.p_rates[i] = (double)this.confusion_mat[3] / this.N;
            }
            else if(i==1){
                this.N = this.confusion_mat[0] + this.confusion_mat[2];
                this.p_rates[i] = (double) this.confusion_mat[0] /this.N;
            }
            else if(i== 2){
                this.N = this.confusion_mat[0]+this.confusion_mat[1];
                this.p_rates[i] = (double) this.confusion_mat[0]/this.N;
            }
            else if(i==3){
                this.N = this.confusion_mat[2]+this.confusion_mat[3];
                this.p_rates[i] = (double) this.confusion_mat[3]/this.N;
            }
            double result[] = this.gemBoundTable.getBound(this.p_rates[i]);
            this.warnbd[i] = result[1];

            this.detectedbd[i] = result[0];


        }
//
//        for(int i =0 ;i<4;i++){
//            System.out.println(this.r_rates[i] + " " + this.warnbd[i] + " " + this.detectedbd[i]);
//        }

        this.isChangeDetected=false;
        this.isWarningZone = false;
        //判断
        boolean flag_warn = true;
        for (int i=0;i<4;i++){

            if(this.r_rates[i] > this.warnbd[i]){
                if(this.isWarningZone == false){
                    this.isWarningZone = true;
                }
                flag_warn = false;
            }

        }
        if(flag_warn){
            this.isWarningZone = false;
        }

        for(int i=0;i<4;i++){
            if(this.r_rates[i] > this.detectedbd[i]){
                this.isChangeDetected= true;
                this.isWarningZone = false;
                break;
            }
        }
    }

    private boolean isInfluenced(int i,int prediction,int trueclass){
        if(prediction == 0 && trueclass == 0){
            if(i==1 || i==2){
                return true;
            }
            else{
                return false;
            }
        }
        if(prediction==0 && trueclass == 1){
            if(i==0 || i == 2){
                return  true;
            }
            else{
                return  false;
            }
        }
        if(prediction == 1 && trueclass ==0){
            if(i==1 || i==3){
                return true;
            }
            else{
                return false;
            }
        }
        if(prediction == 1 && trueclass == 1){
            if(i==0 || i==3){
                return true;
            }
            else{
                return false;
            }
        }
        return false;
    }

    public void getDescription(StringBuilder stringBuilder, int i) {

    }

    protected void prepareForUseImpl(TaskMonitor taskMonitor, ObjectRepository objectRepository) {

    }
}
