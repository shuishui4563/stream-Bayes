/**
 * Created by storm on 27/09/17.
 */
import javafx.util.Pair;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.core.attributeclassobservers.AttributeClassObserver;
import moa.classifiers.core.attributeclassobservers.GaussianNumericAttributeClassObserver;
import moa.classifiers.core.attributeclassobservers.NominalAttributeClassObserver;
import moa.core.AutoExpandVector;
import moa.core.DoubleVector;
import moa.core.Measurement;
import moa.core.StringUtils;
import weka.core.Instance;


public class IncreBayes extends AbstractClassifier {
    private static final long serialVersionUID = 1L;
    protected DoubleVector observedClassDistribution;
    protected AutoExpandVector<AttributeClassObserver> attributeObservers;

    protected Double[] priors;
//    protected AutoExpandVector<PairVector> likelihoods;

    public IncreBayes() {
    }

    public String getPurposeString() {
        return "Naive Bayes classifier: performs classic bayesian prediction while making naive assumption that all inputs are independent.";
    }

    public void resetLearningImpl() {
        this.observedClassDistribution = new DoubleVector();
        this.attributeObservers = new AutoExpandVector();
        this.priors = new Double[2];
        for(int i=0;i<2;i++){
            this.priors[i] = 0.0;
        }
//        this.likelihoods = new AutoExpandVector<PairVector>();
    }

    public void trainOnInstanceImpl(Instance inst) {
        this.observedClassDistribution.addToValue((int)inst.classValue(), inst.weight());

        for(int i = 0; i < inst.numAttributes() - 1; ++i) {
            int instAttIndex = modelAttIndexToInstanceAttIndex(i, inst);
            AttributeClassObserver obs = (AttributeClassObserver)this.attributeObservers.get(i);
            if (obs == null) {
                obs = inst.attribute(instAttIndex).isNominal() ? this.newNominalClassObserver() : this.newNumericClassObserver();
                this.attributeObservers.set(i, obs);
            }

            obs.observeAttributeClass(inst.value(instAttIndex), (int)inst.classValue(), inst.weight());
        }

        double observedClassSum = observedClassDistribution.sumOfValues();

//        double probility = 0.0;
        int inst_class = (int) inst.classValue();
        double w = 2+observedClassSum;
        for(int classIndex = 0; classIndex < 2; ++classIndex){
//            this.priors[classIndex] = this.observedClassDistribution.getValue(classIndex)/observedClassSum;
            if(inst_class == classIndex){
                if(this.priors[classIndex] == 0.0){
                    this.priors[classIndex] = 1.0/w;
                }
                this.priors[classIndex] = w*1.0/(1.0+w)*this.priors[classIndex]+1.0/(1.0+w);
            }
            else{
                this.priors[classIndex] = w*1.0/(1.0+w)*this.priors[classIndex];
            }

        }




    }

    public double[] getVotesForInstance(Instance inst) {
        return doNaiveBayesPrediction(inst, this.priors, this.attributeObservers);
    }

    protected Measurement[] getModelMeasurementsImpl() {
        return null;
    }

    public void getModelDescription(StringBuilder out, int indent) {
        for(int i = 0; i < this.observedClassDistribution.numValues(); ++i) {
            StringUtils.appendIndented(out, indent, "Observations for ");
            out.append(this.getClassNameString());
            out.append(" = ");
            out.append(this.getClassLabelString(i));
            out.append(":");
            StringUtils.appendNewlineIndented(out, indent + 1, "Total observed weight = ");
            out.append(this.observedClassDistribution.getValue(i));
            out.append(" / prob = ");
            out.append(this.observedClassDistribution.getValue(i) / this.observedClassDistribution.sumOfValues());

            for(int j = 0; j < this.attributeObservers.size(); ++j) {
                StringUtils.appendNewlineIndented(out, indent + 1, "Observations for ");
                out.append(this.getAttributeNameString(j));
                out.append(": ");
                out.append(this.attributeObservers.get(j));
            }

            StringUtils.appendNewline(out);
        }

    }

    public boolean isRandomizable() {
        return false;
    }

    protected AttributeClassObserver newNominalClassObserver() {

        return new DIBNomialAttributeObserver();
    }

    protected AttributeClassObserver newNumericClassObserver() {
        return new GaussianNumericAttributeClassObserver();
    }

    public static double[] doNaiveBayesPrediction(Instance inst, Double[] priors, AutoExpandVector<AttributeClassObserver> attributeObservers) {
        double[] votes = new double[priors.length];
//        double observedClassSum = observedClassDistribution.sumOfValues();

        for(int classIndex = 0; classIndex < votes.length; ++classIndex) {
//            votes[classIndex] = observedClassDistribution.getValue(classIndex) / observedClassSum;
            votes[classIndex] = priors[classIndex];

            for(int attIndex = 0; attIndex < inst.numAttributes() - 1; ++attIndex) {
                int instAttIndex = modelAttIndexToInstanceAttIndex(attIndex, inst);
                AttributeClassObserver obs = (AttributeClassObserver)attributeObservers.get(attIndex);
                if (obs != null && !inst.isMissing(instAttIndex)) {
                    votes[classIndex] *= obs.probabilityOfAttributeValueGivenClass(inst.value(instAttIndex), classIndex);
                }
            }
        }

        return votes;
    }

    public static double[] doNaiveBayesPredictionLog(Instance inst, DoubleVector observedClassDistribution, AutoExpandVector<AttributeClassObserver> observers, AutoExpandVector<AttributeClassObserver> observers2) {
        double[] votes = new double[observedClassDistribution.numValues()];
        double observedClassSum = observedClassDistribution.sumOfValues();

        for(int classIndex = 0; classIndex < votes.length; ++classIndex) {
            votes[classIndex] = Math.log10(observedClassDistribution.getValue(classIndex) / observedClassSum);

            for(int attIndex = 0; attIndex < inst.numAttributes() - 1; ++attIndex) {
                int instAttIndex = modelAttIndexToInstanceAttIndex(attIndex, inst);
                AttributeClassObserver obs;
                if (inst.attribute(instAttIndex).isNominal()) {
                    obs = (AttributeClassObserver)observers.get(attIndex);
                } else {
                    obs = (AttributeClassObserver)observers2.get(attIndex);
                }

                if (obs != null && !inst.isMissing(instAttIndex)) {
                    votes[classIndex] += Math.log10(obs.probabilityOfAttributeValueGivenClass(inst.value(instAttIndex), classIndex));
                }
            }
        }

        return votes;
    }

    public void manageMemory(int currentByteSize, int maxByteSize) {
    }
}

