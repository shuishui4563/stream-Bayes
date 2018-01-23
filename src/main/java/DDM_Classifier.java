import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.bayes.NaiveBayes;
import moa.classifiers.core.driftdetection.ChangeDetector;
import moa.classifiers.core.driftdetection.DDM;
import moa.classifiers.core.driftdetection.EDDM;
import moa.classifiers.meta.WEKAClassifier;
import moa.classifiers.rules.RuleClassifierNBayes;
import moa.core.Measurement;
import moa.options.ClassOption;
import weka.core.Instance;
import weka.core.Utils;

/**
 * Created by storm on 01/09/17.
 */
public class DDM_Classifier  extends AbstractClassifier{


    protected Classifier classifier = new DecisionStumpTutorial();

    protected Classifier newclassifier;

    protected ChangeDetector driftDetectionMethod = new EDDM();

    protected boolean newClassifierReset;

    protected int[] confu_mat = new int[4];
    //protected int numberInstances = 0;

    protected int ddmLevel;

    public int warningDetected = 0;

    public int changeDetected = 0;

    public static final int DDM_INCONTROL_LEVEL = 0;

    public static final int DDM_WARNING_LEVEL = 1;

    public static final int DDM_OUTCONTROL_LEVEL = 2;

    DDM_Classifier()
    {
        resetLearningImpl();
    }

    public int[] getConfusion_mat(){
        return this.confu_mat;
    }

    public void resetLearningImpl() {
        this.classifier = new DecisionStumpTutorial();
        this.newclassifier = (DecisionStumpTutorial)this.classifier.copy();
        this.classifier.resetLearning();
        this.newclassifier.resetLearning();
        this.driftDetectionMethod = new EDDM();
        this.newClassifierReset = false;

        for(int i=0;i<4;i++){
            this.confu_mat[i] = 1;
        }

    }

    public void trainOnInstanceImpl(Instance inst) {
//        this.numberInstances++;
        int trueClass = (int) inst.classValue();
        int prediction = Utils.maxIndex(this.classifier.getVotesForInstance(inst));
        int detect_predict = 0;
        if (Utils.maxIndex(this.classifier.getVotesForInstance(inst)) == trueClass) {
            prediction = trueClass;
            detect_predict = 1;
        } else {
            detect_predict = 0;
            if(trueClass == 0){
                prediction = 1;
            }
            else{
                prediction = 0;
            }

        }
//        this.ddmLevel = this.driftDetectionMethod.computeNextVal(prediction);
        this.driftDetectionMethod.input(detect_predict);

        if(trueClass == 0 && prediction == 0){
            this.confu_mat[0]+=1;
        }
        else if(trueClass == 0 && prediction == 1){
            this.confu_mat[2]+=1;
        }
        else if(trueClass == 1 && prediction == 0){
            this.confu_mat[1]+=1;
        }
        else if(trueClass == 1 && prediction == 1){
            this.confu_mat[3]+=1;
        }

        this.ddmLevel = DDM_INCONTROL_LEVEL;
        if (this.driftDetectionMethod.getChange()) {
            this.ddmLevel =  DDM_OUTCONTROL_LEVEL;
        }
        if (this.driftDetectionMethod.getWarningZone()) {
            this.ddmLevel =  DDM_WARNING_LEVEL;
        }
        switch (this.ddmLevel) {
            case DDM_WARNING_LEVEL:

                if (newClassifierReset == true) {
                    this.warningDetected++;
                    this.newclassifier.resetLearning();
                    newClassifierReset = false;
                }
                this.newclassifier.trainOnInstance(inst);
                break;

            case DDM_OUTCONTROL_LEVEL:
                this.changeDetected++;
                this.classifier = null;
                this.classifier = this.newclassifier;
                if (this.classifier instanceof WEKAClassifier) {
                    ((WEKAClassifier) this.classifier).buildClassifier();
                }
                this.newclassifier = new IncreBayes();
                this.newclassifier.resetLearning();
                for(int i=0;i<4;i++){
                    this.confu_mat[i] = 1;
                }
                break;

            case DDM_INCONTROL_LEVEL:
                //System.out.println("0 0 I");
                //System.out.println("DDM_INCONTROL_LEVEL");
                newClassifierReset = true;
                break;
            default:
                System.out.println("ERROR!");

        }

        this.classifier.trainOnInstance(inst);
    }

    protected Measurement[] getModelMeasurementsImpl() {
        return new Measurement[0];
    }

    public void getModelDescription(StringBuilder out, int indent) {

    }

    public boolean isRandomizable() {
        return false;
    }

    public double[] getVotesForInstance(Instance inst) {
        return this.classifier.getVotesForInstance(inst);
    }
}
