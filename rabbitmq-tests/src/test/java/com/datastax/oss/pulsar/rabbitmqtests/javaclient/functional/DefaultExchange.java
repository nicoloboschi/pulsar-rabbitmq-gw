// Copyright (c) 2007-2020 VMware, Inc. or its affiliates.  All rights reserved.
//
// This software, the RabbitMQ Java client library, is triple-licensed under the
// Mozilla Public License 2.0 ("MPL"), the GNU General Public License version 2
// ("GPL") and the Apache License version 2 ("ASL"). For the MPL, please see
// LICENSE-MPL-RabbitMQ. For the GPL, please see LICENSE-GPL2.  For the ASL,
// please see LICENSE-APACHE2.
//
// This software is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
// either express or implied. See the LICENSE file for specific language governing
// rights and limitations of this software.
//
// If you have any questions regarding licensing, please contact us at
// info@rabbitmq.com.

package com.datastax.oss.pulsar.rabbitmqtests.javaclient.functional;

import static org.junit.Assert.fail;

import com.datastax.oss.pulsar.rabbitmqtests.javaclient.BrokerTestCase;
import com.rabbitmq.client.AMQP;
import java.io.IOException;
import org.junit.Test;

public class DefaultExchange extends BrokerTestCase {
  String queueName;

  @Override
  protected void createResources() throws IOException {
    queueName = channel.queueDeclare().getQueue();
  }

  // Publish and declare are the only operations
  // permitted on the default exchange

  @Test
  public void defaultExchangePublish() throws Exception {
    basicPublishVolatile("", queueName); // Implicit binding

    assertDelivered(queueName, 1);
  }

  @Test
  public void bindToDefaultExchange() throws IOException {
    try {
      channel.queueBind(queueName, "", "foobar");
      fail();
    } catch (IOException ioe) {
      checkShutdownSignal(AMQP.ACCESS_REFUSED, ioe);
    }
  }

  @Test
  public void unbindFromDefaultExchange() throws IOException {
    try {
      channel.queueUnbind(queueName, "", queueName);
      fail();
    } catch (IOException ioe) {
      checkShutdownSignal(AMQP.ACCESS_REFUSED, ioe);
    }
  }

  @Test
  public void declareDefaultExchange() throws IOException {
    try {
      channel.exchangeDeclare("", "direct", true);
      fail();
    } catch (IOException ioe) {
      checkShutdownSignal(AMQP.ACCESS_REFUSED, ioe);
    }
  }

  @Test
  public void deleteDefaultExchange() throws IOException {
    try {
      channel.exchangeDelete("");
      fail();
    } catch (IOException ioe) {
      checkShutdownSignal(AMQP.ACCESS_REFUSED, ioe);
    }
  }
}
