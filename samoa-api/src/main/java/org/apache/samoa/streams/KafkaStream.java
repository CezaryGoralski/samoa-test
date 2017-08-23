package org.apache.samoa.streams;

/*
 * #%L
 * SAMOA
 * %%
 * Copyright (C) 2014 - 2017 Apache Software Foundation
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.IOException;
import java.util.Properties;

import org.apache.samoa.instances.Instance;
import org.apache.samoa.instances.Instances;
import org.apache.samoa.instances.InstancesHeader;
import org.apache.samoa.moa.core.Example;
import org.apache.samoa.moa.core.InstanceExample;
import org.apache.samoa.moa.core.ObjectRepository;
import org.apache.samoa.moa.options.AbstractOptionHandler;
import org.apache.samoa.moa.tasks.TaskMonitor;
import org.apache.samoa.streams.kafka.KafkaEntranceProcessor;
import org.apache.samoa.streams.kafka.OosSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javacliparser.IntOption;
import com.github.javacliparser.StringOption;

public class KafkaStream extends AbstractOptionHandler implements InstanceStream {
	
	private static Logger logger = LoggerFactory.getLogger(KafkaStream.class);
	
	public StringOption hostOption = new StringOption("host", 'h', "Kafka host address", "127.0.0.1");
	public StringOption portOption = new StringOption("port", 'p', "Kafka port address", "9092");
	public StringOption topicOption = new StringOption("topic", 't', "Kafka topic name", "samoa_ff");
	public IntOption timeoutOption = new IntOption("timeout", 'e', "Kafka timeout", 1000, 0, Integer.MAX_VALUE);

	/**
	 * 
	 */
	private static final long serialVersionUID = -4387950661589472853L;

	protected Instances instances;
	protected InstanceExample lastInstanceRead;

	protected boolean hitEndOfStream;
	private boolean hasStarted;
	
	private KafkaEntranceProcessor kep;

	@Override
	public InstancesHeader getHeader() {
		return new InstancesHeader(this.instances);
	}

	@Override
	public long estimatedRemainingInstances() {
		return -1;
	}

	@Override
	public boolean hasMoreInstances() {

		return false;
	}

	protected boolean readNextInstance() {
		logger.info("Reading next instance");
		this.instances = kep.getDataset();
		if (this.instances!=null && this.instances.readInstance()) {
			this.lastInstanceRead = new InstanceExample(this.instances.instance(0));
			this.instances.delete(); // keep instances clean
			return true;
		}
		return false;

	}

	@Override
	public Example<Instance> nextInstance() {
		if(this.lastInstanceRead==null) 
			this.readNextInstance();
		return this.lastInstanceRead;
	}

	@Override
	public boolean isRestartable() {
		return true;
	}

	@Override
	public void restart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getDescription(StringBuilder sb, int indent) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository) {
		kep = new KafkaEntranceProcessor(getConsumerProperties(this.hostOption.getValue(), this.portOption.getValue()), this.topicOption.getValue(), this.timeoutOption.getValue(), new OosSerializer());
		kep.onCreate(0);
	}
	
	protected Properties getConsumerProperties(String BROKERHOST, String BROKERPORT) {
        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", BROKERHOST + ":" + BROKERPORT);
        consumerProps.put("enable.auto.commit", "true");
        consumerProps.put("auto.commit.interval.ms", "1000");
        consumerProps.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumerProps.setProperty("group.id", "test");
        consumerProps.setProperty("auto.offset.reset", "earliest");
        return consumerProps;
    }

}
