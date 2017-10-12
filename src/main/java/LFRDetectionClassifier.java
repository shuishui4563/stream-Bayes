import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.active.ActiveClassifier;
import moa.classifiers.bayes.NaiveBayes;
import moa.classifiers.core.driftdetection.ChangeDetector;
import moa.classifiers.core.driftdetection.DDM;
import moa.classifiers.core.driftdetection.EDDM;
import moa.classifiers.core.driftdetection.HDDM_A_Test;
import moa.classifiers.meta.WEKAClassifier;
import moa.classifiers.rules.RuleClassifierNBayes;
import moa.classifiers.trees.HoeffdingAdaptiveTree;
import moa.core.Measurement;
import moa.options.ClassOption;
import weka.core.Instance;
import weka.core.Utils;

import javax.print.attribute.standard.MediaSize;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by storm on 14/08/17.
 */
public class LFRDetectionClassifier extends AbstractClassifier{

    protected DecisionStumpTutorial classifier;

    protected DecisionStumpTutorial newclassifier;

    protected LFR driftDetectionMethod = new LFR();

    protected boolean newClassifierReset;

    protected int lfrLevel;

    protected int[] confusion_mat = new int[4];

    public static final int LFR_INCONTROL_LEVEL = 0;

    public static final int LFR_WARNING_LEVEL = 1;

    public static final int LFR_OUTCONTROL_LEVEL = 2;

    public int changeDetected = 0;

    public int warningDetected = 0;

    public double tpr = 0.0;

    public double ppv = 0.0;

    public int[] getConfusion_mat()
    {
        return this.confusion_mat;
    }

    public void resetLearningImpl() {
        this.classifier = new DecisionStumpTutorial();
        this.newclassifier = (DecisionStumpTutorial) this.classifier.copy();
        this.classifier.resetLearning();
        this.newclassifier.resetLearning();
        this.newClassifierReset = false;
        for(int i=0;i<4;i++){
            this.confusion_mat[i] = 1;
        }

    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {

        // 获得example的真实标签
        int trueClass = (int)inst.classValue();

        int prediction = Utils.maxIndex(this.classifier.getVotesForInstance(inst));

        double renum = prediction + 0.1 * trueClass;

        this.driftDetectionMethod.input(renum);
        this.lfrLevel = LFR_INCONTROL_LEVEL;

        this.confusion_mat = this.driftDetectionMethod.getConfusion_mat();

        if(this.driftDetectionMethod.getChange()==true){
            this.lfrLevel = LFR_OUTCONTROL_LEVEL;
//            System.out.print("reset_level\n");
        }

        if(this.driftDetectionMethod.getWarningZone()){
            this.lfrLevel = LFR_WARNING_LEVEL;
        }


        this.tpr = this.driftDetectionMethod.get_precision();

        this.ppv = this.driftDetectionMethod.get_recall();

//        System.out.println("tpr_ppv:"+"\t"+this.tpr+"\t"+this.ppv);


        switch (this.lfrLevel){
            case LFR_WARNING_LEVEL:
                if(newClassifierReset == true){
                    this.warningDetected++;
                    this.newclassifier.resetLearning();
                    newClassifierReset = false;
                }
                this.newclassifier.trainOnInstance(inst);
                break;
            case LFR_OUTCONTROL_LEVEL:
                this.changeDetected++;
                this.classifier = null;
                this.classifier = this.newclassifier;
                this.newclassifier = new DecisionStumpTutorial();
                this.newclassifier.resetLearning();
                break;
            case LFR_INCONTROL_LEVEL:
                newClassifierReset = true;
                break;
            default:
        }

        this.classifier.trainOnInstance(inst);

    }

    protected Measurement[] getModelMeasurementsImpl() {
        List<Measurement> measurementList = new LinkedList<Measurement>();
        measurementList.add(new Measurement("Change detected", this.changeDetected));
        measurementList.add(new Measurement("Warning detected", this.warningDetected));
        Measurement[] modelMeasurements = (this.classifier).getModelMeasurements();
        if (modelMeasurements != null) {
            for (Measurement measurement : modelMeasurements) {
                measurementList.add(measurement);
            }
        }
        this.changeDetected = 0;
        this.warningDetected = 0;
        return measurementList.toArray(new Measurement[measurementList.size()]);
    }

    public void getModelDescription(StringBuilder stringBuilder, int i) {
        ((AbstractClassifier) this.classifier).getModelDescription(stringBuilder, i);
    }

    public boolean isRandomizable() {
        return true;
    }

    public double[] getVotesForInstance(Instance instance) {
        return this.classifier.getVotesForInstance(instance);
    }
}
