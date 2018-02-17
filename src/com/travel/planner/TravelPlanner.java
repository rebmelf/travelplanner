package com.travel.planner;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/*
 *  When running the class the arguments can be added as: ' "x => z" "y => z" "z=> v" "h =>" "v => h" '
 *  If an error happends, then an IllegalArgumentException is thrown. There are four stages of validation:
 *  1. validateInputLine method: validates if the input format is correct (no logic is checked yet)
 *  2. validateEndpoint method: validates if the endpoint itself is valid based on the logic
 *  3. validateIfCircleExists: validates if adding an endpoint where both the predecessor and the destination are part of the already
 *  planned travel would contain a circle
 *  4. validatePlannedTravel: validates if the planned travel is valid (currently it checks if the planned travel has the same amount of
 *  destinations which we checked, eg. no preecessor is added without it being a real destination)
 */
public class TravelPlanner {

  private static final String SEPARATOR = "=>";

  private final Set<String> existingDestinations = new HashSet<>();
  private final Set<String> independentDestinations = new HashSet<>();
  private final List<String> travel = new LinkedList<>();

  public static void main(final String[] args) {
    final List<String> end = new TravelPlanner().calculate(asList(args));
    System.out.println(end);
  }

  private List<String> calculate(final List<String> args) {
    for (Endpoint endpoint : getEndpoints(args)) {
      validateEndpoint(endpoint);
      final String destination = endpoint.getDestination();
      final Optional<String> predecessor = endpoint.getPredecessor();
      predecessor.ifPresent(p -> handleEndpointWithPredecessor(destination, p));
      if (!predecessor.isPresent()) {
        if (!travel.contains(destination)) {
          independentDestinations.add(destination);
        }
      }
      existingDestinations.add(endpoint.getDestination());
    }
    travel.addAll(independentDestinations);
    validatePlannedTravel();
    return travel;
  }

  private void handleEndpointWithPredecessor(final String destination, final String predecessor) {
    if (!travel.contains(predecessor)) {
      if (!travel.contains(destination)) {
        travel.add(predecessor);
        travel.add(destination);
      } else {
        travel.add(travel.lastIndexOf(destination), predecessor);
      }
    } else if (!travel.contains(destination)) {
      travel.add(travel.lastIndexOf(predecessor) + 1, destination);
    } else {
      validateIfCircleExists(destination, predecessor);
    }
    if (independentDestinations.contains(destination)) {
      independentDestinations.remove(destination);
    }
    if (independentDestinations.contains(predecessor)) {
      independentDestinations.remove(predecessor);
    }
  }

  private List<Endpoint> getEndpoints(final List<String> args) {
    return args.stream()
      .map(this::getEndpoint)
      .collect(toList());
  }

  private Endpoint getEndpoint(final String input) {
    validateInputLine(input);
    final String[] params = input.split(SEPARATOR);
    return params.length == 1
      ? new Endpoint(params[0].trim(), null)
      : new Endpoint(params[0].trim(), params[1].trim());
  }

  private void validateInputLine(final String input) {
    if (input == null || !input.contains(SEPARATOR) || input.trim().startsWith(SEPARATOR)) {
      throw new IllegalArgumentException("Invalid input format");
    }
  }

  private void validateEndpoint(final Endpoint endpoint) {
    if (endpoint.getDestination().equals(endpoint.getPredecessor().orElse(""))) {
      throw new IllegalArgumentException("The travel destination and the predecessor are the same for " + endpoint.getDestination());
    }
    if (existingDestinations.contains(endpoint.getDestination())) {
      throw new IllegalArgumentException("Destination " + endpoint.getDestination() + " is duplicated in the input");
    }
  }

  private void validateIfCircleExists(final String destination, final String predecessor) {
    if (travel.lastIndexOf(predecessor) > travel.lastIndexOf(destination)) {
      throw new IllegalArgumentException("Circle in the plan");
    }
  }

  private void validatePlannedTravel() {
    if (travel.size() != existingDestinations.size()) {
      throw new IllegalArgumentException("No valid travel can be created from the dependencies");
    }
  }

  private class Endpoint {
    final String destination;
    final Optional<String> predecessor;

    Endpoint(final String destination, final String predecessor) {
      this.destination = destination;
      if (predecessor == null || predecessor.isEmpty()) {
        this.predecessor = Optional.empty();
      } else {
        this.predecessor = Optional.of(predecessor);
      }
    }

    String getDestination() {
      return destination;
    }

    Optional<String> getPredecessor() {
      return predecessor;
    }
  }
}
