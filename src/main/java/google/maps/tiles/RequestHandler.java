package google.maps.tiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Objects;
import java.util.function.Consumer;

final class RequestHandler {

    private final AsynchronousSocketChannel channel;
    private final Consumer<InputStream> success;
    private final Consumer<? super Exception> failure;

    RequestHandler(final AsynchronousSocketChannel channel, final Consumer<InputStream> success, final Consumer<? super Exception> failure) {
        assert !Objects.isNull(channel) && !Objects.isNull(success) && !Objects.isNull(failure);

        this.channel = channel;
        this.success = success;
        this.failure = failure;
    }

    AsynchronousSocketChannel getChannel() {
        return this.channel;
    }

    Consumer<InputStream> getSuccess() {
        return this.success;
    }

    Consumer<? super Exception> getFailure() {
        return this.failure;
    }

    void closeChannel() {
        try {
            this.channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void response() {
        final ByteBuffer buffer = ByteBuffer.allocate(1024 * 30);
        this.channel.read(buffer, this, new CompletionHandler<>() {

            @Override
            public void completed(final Integer result, final RequestHandler handler) {
                if (result > 0) {
                    handler.getSuccess().accept(new ByteArrayInputStream(buffer.array()));
                    buffer.clear();

                    RequestHandler.this.channel.read(buffer, handler, this);
                } else if (result < 0) {
                    RequestHandler.this.closeChannel();
                } else {
                    RequestHandler.this.channel.read(buffer, handler, this);
                }
            }

            @Override
            public void failed(final Throwable exc, final RequestHandler handler) {
                handler.getFailure().accept(new Exception(exc));
                RequestHandler.this.closeChannel();
            }
        });
    }


}