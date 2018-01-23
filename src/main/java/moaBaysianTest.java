/**
 * Created by storm on 13/08/17.
 */
import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.bayes.NaiveBayes;
import moa.classifiers.core.driftdetection.DDM;
import moa.classifiers.drift.DriftDetectionMethodClassifier;
import moa.classifiers.drift.SingleClassifierDrift;
import moa.core.InstancesHeader;
import moa.core.Measurement;
import moa.core.ObjectRepository;
import moa.gui.AWTRenderer;
import moa.options.Options;
import moa.streams.generators.LEDGenerator;
import moa.streams.generators.RandomRBFGenerator;
import moa.streams.generators.SEAGenerator;
import moa.tasks.TaskMonitor;
import weka.core.Instance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class moaBaysianTest {
    /*
    测试调用
     */
    public static void main(String[] args) throws IOException {


//        LFRDetectionClassifier learner = new LFRDetectionClassifier();

//        DDM_Classifier learner = new DDM_Classifier();
        IncreBayes learner = new IncreBayes();
//        NaiveBayes learner = new NaiveBayes();
//        DecisionStumpTutorial learner = new DecisionStumpTutorial();
        //定义stream
        LfrConceptDriftStream stream =  new LfrConceptDriftStream();
//        SEAGenerator stream = new SEAGenerator();
        stream.prepareForUse();
        learner.setModelContext(stream.getHeader());
        learner.prepareForUse();

        double accuracy = 0.0;
        int numberSamplesCorrect = 0;
        int numberSamples = 0;
        boolean isTesting = true;

        File data_acc = new File("/home/sue/Result/HRTU_Incre.txt");
        data_acc.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(data_acc));

        int[] confu_mat = new int[4];
        while(stream.hasMoreInstances()){
            Instance trainInst = stream.nextInstance();
            if(isTesting){

                if(learner.correctlyClassifies(trainInst)){
                    numberSamplesCorrect++;
                }
                numberSamples++;
                accuracy = 100.0 * (double) numberSamplesCorrect/(double) numberSamples;
//                confu_mat = learner.getConfusion_mat();
//                out.write(String.format("%.2f\t%d\t%d\t%d\t%d\t%d\n",accuracy,numberSamples,confu_mat[0],confu_mat[1],confu_mat[2],confu_mat[3]));
//                out.flush();
                out.write(String.format("%d\t%.2f\n",numberSamples,accuracy));
                out.flush();
            }

            learner.trainOnInstance(trainInst);
        }

    }

}
