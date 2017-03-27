# awsimandias

A clojure application that runs _amazonica_ calls in manaifold futures.

## Goals

- Testability
- Availability
- Insight into the application via metrics
- Make clients use mutually authenticated TLS

## Testing strategy

1. The http application allows only authorized users.
2. The http application returns responses with known shapes.
3. The boto queries conform to expectations - these aren't end to end
   application/boto tests, these test that given some trusted, verified mock of
   AWS (possibly a mock-server, possibly moto) the boto calls "work".


## Benchmarking

To run benchmark(s) you must have the following environment variables set:

* AWS_SECRET_KEY
* AWS_ACCESS_KEY

The benchmark will call the code to be benchmarked ~60 to 100 times. This will
hit AWS but these are read only calls.

Use `lein perforate` to benchmark.

## License

Copyright © 2016 Chris Wolfe

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
