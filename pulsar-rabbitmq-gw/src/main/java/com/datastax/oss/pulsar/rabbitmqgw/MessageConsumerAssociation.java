/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.pulsar.rabbitmqgw;

import java.util.concurrent.CompletableFuture;
import org.apache.pulsar.client.api.MessageId;

public final class MessageConsumerAssociation {
  private final MessageId messageId;
  private final AMQConsumer consumer;
  private final boolean usesCredit;
  private final PulsarConsumer pulsarConsumer;
  private final int size;

  MessageConsumerAssociation(
      MessageId messageId,
      AMQConsumer consumer,
      boolean usesCredit,
      PulsarConsumer pulsarConsumer,
      int size) {
    this.messageId = messageId;
    this.consumer = consumer;
    this.usesCredit = usesCredit;
    this.pulsarConsumer = pulsarConsumer;
    this.size = size;
  }

  public MessageId getMessageId() {
    return messageId;
  }

  public int getSize() {
    return size;
  }

  public AMQConsumer getConsumer() {
    return consumer;
  }

  public boolean isUsesCredit() {
    return usesCredit;
  }

  public CompletableFuture<Void> ack() {
    return pulsarConsumer.ackMessage(messageId);
  }

  public void requeue() {
    pulsarConsumer.nackMessage(messageId);
  }
}
