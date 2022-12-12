
## 1. add common dependencies for grpc project

```xml
        <dependency>
            <groupId>com.google.protobuf</groupId>
            <artifactId>protobuf-java</artifactId>
            <version>3.11.4</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
            <version>1.27.0</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-protobuf</artifactId>
            <version>1.27.0</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-stub</artifactId>
            <version>1.27.0</version>
        </dependency>
```

## 2. add plugins to convert protobuf file to java.

Those java code are like interfaces of services which will be implemented. 

### 2.1 the compiler which convert prototype files to java files

```xml
                            <outputTarget>
                                <type>grpc-java</type>
                                <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.15.0</pluginArtifact>
                                <outputDirectory>src/main/java</outputDirectory>
                            </outputTarget>
```
### 2.2 where to find the generated java files
```xml
                            <outputTarget>
                                <type>java</type>
                                <outputDirectory>src/main/java</outputDirectory>
                            </outputTarget>
```

### 2.3 where to find prototype files
```xml
                            <inputDirectories>
                                <include>src/main/resources</include>
                            </inputDirectories>
```

## 3. create protobuf file

The proto file which will be treated as a service interface after converted to java file

`syntex = "proto3"`

The output java file name, say `User.java`

`option java_outer_classname = "User";`

java package name

`option java_package = "nd.pemo.grpc";`

define service, e.g. service interface. You should implement this interface to process the service.

`service user {}`

define method with return type rpc(Remote Procedure Call)

`rpc login(LoginRequest) returns (APIResponse);`

define message format that will be exchanged between client and server

```protobuf
message LoginRequest {
  string username = 1;  // the number is used by grpc framework for the protocol buffers for the backward compatibility
  string password = 2;
}
```

## 4. implementing interface e.g. service

`public class UserService extends userGrpc.userImplBase {}`

### 4.1 implementing apis

`interface StreamObserver<V>` Receives notifications from an observable stream of messages.
> It is used by both the client stubs and service implementations for sending or receiving stream messages.

> For outgoing messages, a StreamObserver is provided by the GRPC library to the application. 
> 
> For incoming messages, the application implements the StreamObserver and passes it to the GRPC library for receiving.

> Since individual StreamObservers are not thread-safe, if multiple threads will be writing to a StreamObserver concurrently, the application must synchronize calls.


```java
@Override
public void login(User.LoginRequest request, StreamObserver<User.APIResponse> responseObserver) {}

```


receive message

`User.LoginRequest request` receive messages e.g. parameters or data.

create the response
`User.APIResponse.Builder response = User.APIResponse.newBuilder();`

and construct the response based on message format
`responseBuilder.setResponseCode(1).setResponsemessage("Login Success");`

wrap the response object inside the streamObserver. 
And send the streamObserver back to client.
So that the client knows how to read response streamObserver object
and extract the response.

`responseStreamObserver.onNext(response.build());`

`onNext(V value)` Receives a value from the stream.

> Can be called many times but is never called after onError(Throwable) or onCompleted() are called.

> Unary calls must invoke onNext at most once. 

> Clients may invoke onNext at most once for server streaming calls, 
> but may receive many onNext callbacks. 

> Servers may invoke onNext at most once for client streaming calls, 
> but may receive many onNext callbacks.

Finally, closing the connection between server api and client 
by using `responseStreamObserver.onCompleted()`.

## 5. create server

use the **server builder** which is part of grpc library

setting the port and making the service embedded inside the server

`ServerBuilder.forPort(9091).addService(new UserService()).build();`

using `awaitTermination()` to keep server alive

## 6. Testing Server. 
Checking whether grpc server is running.

testing with BloomRPC Client App (It's like postman tool.)

 - import `userproto.proto` file to BloomPRC.
 - setting server port `localhost：9091`
 - sending request to service (e.g. interface) including login api and logout api with corresponding message format (the concepts, service, api, interface are similar in this context)
 

## 7. lookupMethod

In `grpc-core-1.27.0.jar!/io/grpc/internal/ServerImpl.class`

```java
      final class MethodLookup extends ContextRunnable {
        MethodLookup() {
            super(context);
        }
    ...
        private void runInternal() {
          ServerMethodDefinition<?, ?> wrapMethod;
          ServerCallParameters<?, ?> callParams;
          try {
            ServerMethodDefinition<?, ?> method = registry.lookupMethod(methodName);
            if (method == null) {
              method = fallbackRegistry.lookupMethod(methodName, stream.getAuthority());
            }
```

When the client connect server successfully, the server will look up the corresponding 
service which the client requests.

## 8. An example

### sample 1 envoy
#### proto

`api-0.1.35.jar!/envoy/service/discovery/v3/ads.proto`

```protobuf
  rpc StreamAggregatedResources(stream DiscoveryRequest) returns (stream DiscoveryResponse) {
  }
```

#### Generated codes
`api-0.1.35.jar!/io/envoyproxy/envoy/service/discovery/v3/AggregatedDiscoveryService.class`

```java

/**
 * <pre>
 * See https://github.com/envoyproxy/envoy-api#apis for a description of the role of
 * ADS and how it is intended to be used by a management server. ADS requests
 * have the same structure as their singleton xDS counterparts, but can
 * multiplex many resource types on a single stream. The type_url in the
 * DiscoveryRequest/DiscoveryResponse provides sufficient information to recover
 * the multiplexed singleton APIs at the Envoy instance and management server.
 * </pre>
 */
public static abstract class AggregatedDiscoveryServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * This is a gRPC-only API.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest> streamAggregatedResources(
            io.grpc.stub.StreamObserver<io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse> responseObserver) {
        return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getStreamAggregatedResourcesMethod(), responseObserver);
    }

```

#### Implementing service

```java
  /**
   * Returns an ADS implementation that uses this server's {@link ConfigWatcher}.
   */
  public AggregatedDiscoveryServiceImplBase getAggregatedDiscoveryServiceImpl() {
    return new AggregatedDiscoveryServiceImplBase() {
      @Override
      public StreamObserver<DiscoveryRequest> streamAggregatedResources(
          StreamObserver<DiscoveryResponse> responseObserver) {

        return createRequestHandler(responseObserver, true, ANY_TYPE_URL);
      }
      
```

### sample 2 RouteGuide

#### proto
[bi-direction stream](https://github.com/grpc/grpc-java/blob/v1.48.1/examples/src/main/proto/route_guide.proto#L51)
```protobuf
rpc RouteChat(stream RouteNote) returns (stream RouteNote) {}
```

#### Implementing
```java

  private static class RouteGuideService extends RouteGuideGrpc.RouteGuideImplBase {

      ...


      /**
       * Gets a stream of points, and responds with statistics about the "trip": number of points,
       * number of known features visited, total distance traveled, and total time spent.
       *
       * @param responseObserver an observer to receive the response summary.
       * @return an observer to receive the requested route points.
       */
      @Override
      public StreamObserver<Point> recordRoute(final StreamObserver<RouteSummary> responseObserver) {
          return new StreamObserver<Point>() {
              int pointCount;
              int featureCount;
              int distance;
              Point previous;
              final long startTime = System.nanoTime();

              @Override
              public void onNext(Point point) {
                  pointCount++;
                  if (RouteGuideUtil.exists(checkFeature(point))) {
                      featureCount++;
                  }
                  // For each point after the first, add the incremental distance from the previous point to
                  // the total distance value.
                  if (previous != null) {
                      distance += calcDistance(previous, point);
                  }
                  previous = point;
              }

              @Override
              public void onError(Throwable t) {
                  logger.log(Level.WARNING, "recordRoute cancelled");
              }

              @Override
              public void onCompleted() {
                  long seconds = NANOSECONDS.toSeconds(System.nanoTime() - startTime);
                  responseObserver.onNext(RouteSummary.newBuilder().setPointCount(pointCount)
                          .setFeatureCount(featureCount).setDistance(distance)
                          .setElapsedTime((int) seconds).build());
                  responseObserver.onCompleted();
              }
          };
      }

```

## CallStreamObserver

```java

/**
 * A refinement of StreamObserver provided by the GRPC runtime to the application (the client or
 * the server) that allows for more complex interactions with call behavior.
 *
 * <p>In any call there are logically four {@link StreamObserver} implementations:
 * <ul>
 *   <li>'inbound', client-side - which the GRPC runtime calls when it receives messages from
 *   the server. This is implemented by the client application and passed into a service method
 *   on a stub object.
 *   </li>
 *   <li>'outbound', client-side - which the GRPC runtime provides to the client application and the
 *   client uses this {@code StreamObserver} to send messages to the server.
 *   </li>
 *   <li>'inbound', server-side - which the GRPC runtime calls when it receives messages from
 *   the client. This is implemented by the server application and returned from service
 *   implementations of client-side streaming and bidirectional streaming methods.
 *   </li>
 *   <li>'outbound', server-side - which the GRPC runtime provides to the server application and
 *   the server uses this {@code StreamObserver} to send messages (responses) to the client.
 *   </li>
 * </ul>
 *
 * <p>Implementations of this class represent the 'outbound' message streams. The client-side
 * one is {@link ClientCallStreamObserver} and the service-side one is
 * {@link ServerCallStreamObserver}.
 *
 * <p>Like {@code StreamObserver}, implementations are not required to be thread-safe; if multiple
 * threads will be writing to an instance concurrently, the application must synchronize its calls.
 *
 * <p>DO NOT MOCK: The API is too complex to reliably mock. Use InProcessChannelBuilder to create
 * "real" RPCs suitable for testing.
 *
 * @param <V> type of outbound message.
 */
@ExperimentalApi("https://github.com/grpc/grpc-java/issues/8499")
```

## Ref

[grpc server cpp demo](https://github.com/ppdouble/grpc-cpp-server-sample)

[grpc client cpp demo](https://github.com/ppdouble/grpc-cpp-client-sample)

[grpc client with springboot demo](https://github.com/ppdouble/springboot-grpc-client-sample)

[grpc server with springboot demo](https://github.com/ppdouble/springboot-grpc-server-sample)

[grpc client java demo](https://github.com/ppdouble/grpc-service-sample-client)

[grpc server java demo](https://github.com/ppdouble/grpc-service-pemo)

[sprintboot with grpc server. Fork from nils](https://github.com/ppdouble/city-score)

[gRPC Java Client App Implementation](https://www.youtube.com/watch?v=J0AMX9YpdLk)

[grpc sample on github](https://github.com/techtter/grpc)

Comment of source code `grpc-stub-1.27.0.source.jar!/io/grpc/stub/StreamObserver.java`
