import moa.classifiers.core.AttributeSplitSuggestion;
import moa.classifiers.core.attributeclassobservers.DiscreteAttributeClassObserver;
import moa.classifiers.core.conditionaltests.NominalAttributeBinaryTest;
import moa.classifiers.core.conditionaltests.NominalAttributeMultiwayTest;
import moa.classifiers.core.splitcriteria.SplitCriterion;
import moa.core.AutoExpandVector;
import moa.core.DoubleVector;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.tasks.TaskMonitor;
import weka.core.Utils;

/**
 * 用于DIB 分类器离散属性数据分类
 * Created by storm on 23/01/18.
 */
public class DIBNomialAttributeObserver extends AbstractOptionHandler implements
        DiscreteAttributeClassObserver{
    private static final long serialVersionUID = 1L;

    protected double totalWeightObserved = 0.0;

    protected double missingWeightObserved = 0.0;

    public AutoExpandVector<DoubleVector> attValDistPerClass = new AutoExpandVector<DoubleVector>();

    @Override
    public void observeAttributeClass(double attVal, int classVal, double weight) {
        //classVal
        if (Utils.isMissingValue(attVal)) {
            this.missingWeightObserved += weight;
        } else {
            int attValInt = (int) attVal;
            DoubleVector valDist = this.attValDistPerClass.get(classVal);
            if (valDist == null) {
                valDist = new DoubleVector();
                this.attValDistPerClass.set(classVal, valDist);
            }
            valDist.addToValue(attValInt, weight);
        }
        this.totalWeightObserved += weight;
    }

    @Override
    public double probabilityOfAttributeValueGivenClass(double attVal,
                                                        int classVal) {
        DoubleVector obs = this.attValDistPerClass.get(classVal);
        return obs != null ? (obs.getValue((int) attVal) + 2.0)
                / (obs.sumOfValues() + obs.numValues()) : 1.0/ (obs.sumOfValues() + obs.numValues());
    }

    public double totalWeightOfClassObservations() {
        return this.totalWeightObserved;
    }

    public double weightOfObservedMissingValues() {
        return this.missingWeightObserved;
    }

    @Override
    public AttributeSplitSuggestion getBestEvaluatedSplitSuggestion(
            SplitCriterion criterion, double[] preSplitDist, int attIndex,
            boolean binaryOnly) {
        AttributeSplitSuggestion bestSuggestion = null;
        int maxAttValsObserved = getMaxAttValsObserved();
        if (!binaryOnly) {
            double[][] postSplitDists = getClassDistsResultingFromMultiwaySplit(maxAttValsObserved);
            double merit = criterion.getMeritOfSplit(preSplitDist,
                    postSplitDists);
            bestSuggestion = new AttributeSplitSuggestion(
                    new NominalAttributeMultiwayTest(attIndex), postSplitDists,
                    merit);
        }
        for (int valIndex = 0; valIndex < maxAttValsObserved; valIndex++) {
            double[][] postSplitDists = getClassDistsResultingFromBinarySplit(valIndex);
            double merit = criterion.getMeritOfSplit(preSplitDist,
                    postSplitDists);
            if ((bestSuggestion == null) || (merit > bestSuggestion.merit)) {
                bestSuggestion = new AttributeSplitSuggestion(
                        new NominalAttributeBinaryTest(attIndex, valIndex),
                        postSplitDists, merit);
            }
        }
        return bestSuggestion;
    }

    public int getMaxAttValsObserved() {
        int maxAttValsObserved = 0;
        for (DoubleVector attValDist : this.attValDistPerClass) {
            if ((attValDist != null)
                    && (attValDist.numValues() > maxAttValsObserved)) {
                maxAttValsObserved = attValDist.numValues();
            }
        }
        return maxAttValsObserved;
    }

    public double[][] getClassDistsResultingFromMultiwaySplit(
            int maxAttValsObserved) {
        DoubleVector[] resultingDists = new DoubleVector[maxAttValsObserved];
        for (int i = 0; i < resultingDists.length; i++) {
            resultingDists[i] = new DoubleVector();
        }
        for (int i = 0; i < this.attValDistPerClass.size(); i++) {
            DoubleVector attValDist = this.attValDistPerClass.get(i);
            if (attValDist != null) {
                for (int j = 0; j < attValDist.numValues(); j++) {
                    resultingDists[j].addToValue(i, attValDist.getValue(j));
                }
            }
        }
        double[][] distributions = new double[maxAttValsObserved][];
        for (int i = 0; i < distributions.length; i++) {
            distributions[i] = resultingDists[i].getArrayRef();
        }
        return distributions;
    }

    public double[][] getClassDistsResultingFromBinarySplit(int valIndex) {
        DoubleVector equalsDist = new DoubleVector();
        DoubleVector notEqualDist = new DoubleVector();
        for (int i = 0; i < this.attValDistPerClass.size(); i++) {
            DoubleVector attValDist = this.attValDistPerClass.get(i);
            if (attValDist != null) {
                for (int j = 0; j < attValDist.numValues(); j++) {
                    if (j == valIndex) {
                        equalsDist.addToValue(i, attValDist.getValue(j));
                    } else {
                        notEqualDist.addToValue(i, attValDist.getValue(j));
                    }
                }
            }
        }
        return new double[][]{equalsDist.getArrayRef(),
                notEqualDist.getArrayRef()};
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository) {
        // TODO Auto-generated method stub
    }

    @Override
    public void observeAttributeTarget(double attVal, double target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
