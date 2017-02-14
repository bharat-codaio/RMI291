package conformance2;

import conformance2.rmi.*;
import test.*;

/** Runs all conformance tests on distributed filesystem components.

    <p>
    Tests performed are:
    <ul>
    <li>{@link conformance.rmi.SkeletonTest}</li>
    <li>{@link StubTest}</li>
    <li>{@link conformance.rmi.ConnectionTest}</li>
    <li>{@link conformance.rmi.ThreadTest}</li>
    </ul>
 */
public class ConformanceTests
{
    /** Runs the tests.

        @param arguments Ignored.
     */
    public static void main(String[] arguments)
    {
        // Create the test list, the series object, and run the test series.
        @SuppressWarnings("unchecked")
        Class<? extends Test>[]     tests =
            new Class[] {
                CallTest.class,
                         ArgumentTest.class,
                         ReturnTest.class,
                         ExceptionTest.class,
                         CompleteCallTest.class,
                         ImplicitStubCallTest.class,
                         NullTest.class,
                         RemoteInterfaceTest.class,
                         ListenTest.class,
                         RestartTest.class,
                         NoAddressTest.class,
                         ServiceErrorTest.class,
                         StubTest.class,
                         EqualsTest.class,
                         HashCodeTest.class,
                         ToStringTest.class,
                         SerializableTest.class,
                         OverloadTest.class,
                         ShadowTest.class,
                         InheritanceTest.class,
                         SubclassTest.class,
                         SecurityTest.class,
                         ThreadTest.class
            };

        Series                      series = new Series(tests);
        SeriesReport                report = series.run(3, System.out);

        // Print the report and exit with an appropriate exit status.
        report.print(System.out);
        System.exit(report.successful() ? 0 : 2);
    }
}
