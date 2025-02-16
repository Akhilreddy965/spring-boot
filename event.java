import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

// Event class
class Event {
    private final String type;
    private final String timestamp;

    public Event(String type) {
        this.type = type;
        this.timestamp = new Date().toString();
    }

    public String getType() {
        return type;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

// Observer interface
interface Observer {
    void onEvent(Event event);
}

// Concrete Observer
class UserLoginObserver implements Observer {
    @Override
    public void onEvent(Event event) {
        if (event.getType().equals("UserLoginEvent")) {
            System.out.println("UserLoginObserver: Handling User Login Event at " + event.getTimestamp());
        }
    }
}

class FileUploadObserver implements Observer {
    @Override
    public void onEvent(Event event) {
        if (event.getType().equals("FileUploadedEvent")) {
            System.out.println("FileUploadObserver: Handling File Upload Event at " + event.getTimestamp());
        }
    }
}

// EventPublisher
class EventPublisher {
    private final Map<String, List<Observer>> observers = new HashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void registerObserver(String eventType, Observer observer) {
        observers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(observer);
    }

    public void unregisterObserver(String eventType, Observer observer) {
        List<Observer> observerList = observers.get(eventType);
        if (observerList != null) {
            observerList.remove(observer);
        }
    }

    public void publishEvent(Event event) {
        List<Observer> eventObservers = observers.get(event.getType());
        if (eventObservers != null) {
            for (Observer observer : eventObservers) {
                executor.submit(() -> observer.onEvent(event)); // Process event asynchronously
            }
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}

// Main Class
public class EventDrivenSystem {
    public static void main(String[] args) {
        EventPublisher publisher = new EventPublisher();

        // Create observers
        Observer loginObserver = new UserLoginObserver();
        Observer fileObserver = new FileUploadObserver();

        // Register observers for specific events
        publisher.registerObserver("UserLoginEvent", loginObserver);
        publisher.registerObserver("FileUploadedEvent", fileObserver);

        // Simulate event publishing
        publisher.publishEvent(new Event("UserLoginEvent"));
        publisher.publishEvent(new Event("FileUploadedEvent"));

        // Wait a bit for async processing
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        publisher.shutdown();
    }
}
