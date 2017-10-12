import moa.core.InstancesHeader;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.streams.ArffFileStream;
import moa.streams.InstanceStream;
import moa.streams.generators.SEAGenerator;
import moa.tasks.TaskMonitor;
import weka.core.Instance;

import java.util.Random;

/**
 * Created by storm on 20/08/17.
 * 生成concept drift stream的模拟数据流
 */

public class LfrConceptDriftStream extends AbstractOptionHandler implements
        InstanceStream {

    protected ArffFileStream inputStream;
//    protected ArffFileStream driftStream;
//    protected Random random;
    protected int numberInstanceNum;
//    protected double alpha;
//    protected int width;
//    protected int pos; //发生concept drift的位置

    public LfrConceptDriftStream(){

    }


    protected void prepareForUseImpl(TaskMonitor taskMonitor, ObjectRepository objectRepository) {
        this.inputStream = new ArffFileStream("/home/sue/Documents/stream data classification/毕业论文/dataset/usenet1.arff",-1);
        this.inputStream.prepareForUseImpl(taskMonitor,objectRepository);

    }

    public InstancesHeader getHeader() {
        return this.inputStream.getHeader();
    }

    public long estimatedRemainingInstances() {
        return this.inputStream.estimatedRemainingInstances() ;
//        + this.driftStream.estimatedRemainingInstances();
    }

    public boolean hasMoreInstances() {
        return this.inputStream.hasMoreInstances();
//                || this.driftStream.hasMoreInstances();
    }

    public Instance nextInstance() {
        this.numberInstanceNum ++;
        return this.inputStream.nextInstance();

    }

    public boolean isRestartable() {

        return this.inputStream.isRestartable() ;
//        && this.driftStream.isRestartable();
    }

    public void restart() {
        this.inputStream.restart();
        this.numberInstanceNum = 0;
    }

    public void getDescription(StringBuilder stringBuilder, int i) {


    }
}
