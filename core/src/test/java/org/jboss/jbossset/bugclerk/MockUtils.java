package org.jboss.jbossset.bugclerk;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.jbossset.bugclerk.aphrodite.AphroditeClient;
import org.jboss.jbossset.bugclerk.checks.utils.DateUtils;
import org.jboss.jbossset.bugclerk.utils.URLUtils;
import org.jboss.set.aphrodite.config.TrackerType;
import org.jboss.set.aphrodite.domain.Codebase;
import org.jboss.set.aphrodite.domain.Comment;
import org.jboss.set.aphrodite.domain.Flag;
import org.jboss.set.aphrodite.domain.FlagStatus;
import org.jboss.set.aphrodite.domain.Issue;
import org.jboss.set.aphrodite.domain.IssueEstimation;
import org.jboss.set.aphrodite.domain.IssueStatus;
import org.jboss.set.aphrodite.domain.IssueType;
import org.jboss.set.aphrodite.domain.PullRequest;
import org.jboss.set.aphrodite.domain.PullRequestState;
import org.jboss.set.aphrodite.domain.Release;
import org.jboss.set.aphrodite.domain.Repository;
import org.jboss.set.aphrodite.domain.Stage;
import org.jboss.set.aphrodite.domain.Stream;
import org.jboss.set.aphrodite.domain.StreamComponent;
import org.jboss.set.aphrodite.domain.User;
import org.jboss.set.aphrodite.issue.trackers.jira.JiraIssue;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public final class MockUtils {

    private static final String TRACKER_URL_PREFIX = "https://bugzilla.redhat.com/show_bug.cgi?id=";
    private static final String JIRA_TRACKER_URL_PREFIX = "https://issues.jboss.org/browse/";

    private MockUtils() {
    }

    private static URL buildUrlFromIssueId(final String id) {
        try {
            return new URL(TRACKER_URL_PREFIX + id);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static URL buildJiraUrlFromId(final String id) {
        try {
            return new URL(JIRA_TRACKER_URL_PREFIX + id);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }

    }

    public static Comment mockComment(String id, String text, String bugId) {
        Comment mock = Mockito.mock(Comment.class);
        Mockito.when(mock.getId()).thenReturn(Optional.of(bugId));
        Mockito.when(mock.getBody()).thenReturn(text);
        return mock;
    }

    public static List<Comment> mockCommentsWithOneItem(String id, String text, String bugId) {
        List<Comment> comments = new ArrayList<Comment>();
        comments.add(mockComment(id, text, bugId));
        return comments;
    }

    public static URL buildURL(String id) {
        try {
            return new URL(TRACKER_URL_PREFIX + id);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URL buildURL(String trackerUrlPrefix, String id) {
        try {
            return new URL(trackerUrlPrefix + "/" + id);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }


    public static Issue mockBzIssue(String bugId, String summary) {
        return mockBzIssue(bugId, buildURL(bugId), summary);
    }

    public static Issue mockBzIssue(String bugId, URL bugURL, String summary) {
        Issue mock = populateMock(bugId, bugURL, summary, createMockStub(TrackerType.BUGZILLA));
        List<Release> releases = mockReleases("6.4.0","");
        Mockito.when(mock.getReleases()).thenReturn(releases);
        return mock;
    }

    public static JiraIssue mockJiraIssue(String bugId, String summary) {
        return mockJiraIssue(bugId, buildJiraUrlFromId(bugId), summary);
    }

    public static JiraIssue mockJiraIssue(String bugId, URL bugURL, String summary) {
       JiraIssue mock = (JiraIssue) populateMock(bugId, bugURL, summary, createMockStub(TrackerType.JIRA));
        List<Release> releases = mockReleases("6.4.0","");
        Mockito.when(mock.getReleases()).thenReturn(releases);
        Mockito.when(mock.getSprintRelease()).thenReturn("");
        Mockito.when(mock.getStreamStatus()).thenReturn(new HashMap<>());
        return mock;
    }

    private static Issue mockTrackerType(Issue issue, TrackerType type) {
        Mockito.when(issue.getTrackerType()).thenReturn(type);
        return issue;
    }

    private static Issue createMockStub(TrackerType type) {
        switch(type) {
            case JIRA:
                return mockTrackerType(Mockito.mock(JiraIssue.class), TrackerType.JIRA);
            case BUGZILLA:
            default:
                return mockTrackerType(Mockito.mock(Issue.class), TrackerType.BUGZILLA);
        }
    }

    public static <T extends Issue> T populateMock(String bugId, URL bugURL, String summary, T mock) {
        final Optional<IssueEstimation> estimation = Optional.of(mockEstimation(8));

        Mockito.when(mock.getTrackerId()).thenReturn(Optional.of(bugId));
        Mockito.when(mock.getURL()).thenReturn(bugURL);
        Mockito.when(mock.getSummary()).thenReturn(Optional.of(summary));
        Mockito.when(mock.getType()).thenReturn(IssueType.BUG);
        Mockito.when(mock.getEstimation()).thenReturn(estimation);
        Mockito.when(mock.getStatus()).thenReturn(IssueStatus.NEW);
        Mockito.when(mock.getStage()).thenReturn(mockStage());
        Mockito.when(mock.getLastUpdated()).thenReturn(Optional.of(DateUtils.threeWeeksAgo()));
        Mockito.when(mock.getCreationTime()).thenReturn(Optional.of(DateUtils.threeMonthAgo()));
        Mockito.when(mock.getAssignee()).thenReturn(Optional.of(User.createWithEmail("jboss-set@redhat.com")));
        Mockito.when(mock.getReporter()).thenReturn(Optional.of(User.createWithEmail("Romain Pelisse <belaran@redhat.com>")));
        Mockito.when(mock.getProduct()).thenReturn(Optional.ofNullable(null));
        return mock;
    }

    public static List<Release> mockReleases(String releaseVersion, String milestone) {
        List<Release> releases = new ArrayList<Release>(1);
        releases.add(new Release(releaseVersion,milestone));
        return releases;
    }

    private static Stage mockStage() {
        Stage stage = new Stage();
        stage.getStateMap().put(Flag.PM, FlagStatus.NO_SET);
        stage.getStateMap().put(Flag.DEV, FlagStatus.NO_SET);
        stage.getStateMap().put(Flag.QE, FlagStatus.NO_SET);
        return stage;
    }

    public static Violation mockViolation(final String bugId, final String checkname) {
        Violation mock = beginMockingViolation(bugId, checkname);
        Mockito.when(mock.getLevel()).thenReturn(Severity.MINOR);
        return mock;
    }


    private static Violation beginMockingViolation(String bugId, String checkname) {
        Violation mock = Mockito.mock(Violation.class);
        Mockito.when(mock.getCheckName()).thenReturn(checkname);
        Mockito.when(mock.getMessage()).thenReturn("Message for " + checkname + ".");
        return mock;
    }

	public static Violation mockViolationWithSeverity(String bugId, String checkname,
			Severity severity) {
        Violation mock = beginMockingViolation(bugId, checkname);
        Mockito.when(mock.getLevel()).thenReturn(severity);
		return mock;
	}



    public static List<Violation> mockViolationsListWithOneItem(final String bugId, final String checkname) {
        List<Violation> violations = new ArrayList<Violation>(1);
        violations.add(mockViolation(bugId, checkname));
        return violations;
    }

    private static IssueEstimation mockEstimation(double estimate) {
        IssueEstimation estimation = Mockito.mock(IssueEstimation.class);
        Mockito.when(estimation.getInitialEstimate()).thenReturn(estimate);
        return estimation;
    }

    public static List<URL> idsAsURLs(String... ids) {
        List<URL> urls = new ArrayList<>();
        for (String id : ids) {
            urls.add(buildUrlFromIssueId(id));
        }
        return urls;
    }

    public static  List<Issue> generateMockIssues(int nbIssue, String idPrefix, String summaryPrefix) {
       List<Issue> issues = new ArrayList<>(nbIssue);
        for ( int i = 1; i < (nbIssue + 1); i++ )
            issues.add(MockUtils.mockBzIssue(idPrefix + i, summaryPrefix + i));
        return issues;
    }

    public static List<Violation> generateMockViolationsForIssue(String bugId, String... checknames) {
        List<Violation> violations = new ArrayList<Violation>(checknames.length);
        for ( String checkname : checknames )
            violations.add(mockViolation(bugId, checkname));
        return violations;
    }

    public static Map<String,FlagStatus> mockStreamStatus(String streamName, FlagStatus flagStatus) {
        Map<String,FlagStatus> map = new HashMap<String,FlagStatus>(0);
        map.put(streamName, flagStatus);
        return map;
    }

    public static List<Stream> mockStreamsWithStreamWithOneComponent(String streamName, String componentName, String URI, String branchName, String tag, String version, String gav ) {
        return Arrays.asList(MockUtils.mockStreamWithOneComponent(streamName, componentName, URI, branchName, tag, version, gav));
    }

    public static List<URL> mockPullRequestsUrls(String pullRequestURI) {
        return Arrays.asList(URLUtils.createURLFromString(pullRequestURI));
    }

    public static List<Release> mockReleases(String targetRelease) {
        Release release = new Release();
        release.setVersion(targetRelease);
        return Arrays.asList(release);
    }

    public static Stream mockStreamWithOneComponent(String streamName, String componentName, String URI, String branchName, String tag, String version, String gav ) {
        Stream stream = new Stream(streamName);
        Codebase codebase = new Codebase(branchName);
        List<String> contacts = new ArrayList<String>(0);
        StreamComponent component = new StreamComponent(componentName,
                contacts,URLUtils.createURIFromString(URI),
                codebase, tag, version, gav);
        stream.addComponent(component);
        return stream;
    }

    public static PullRequest mockPullRequest(final String url, final String title, final String branch, final PullRequestState state) throws MalformedURLException {
        final PullRequest pullRequest = Mockito.mock(PullRequest.class);
        Mockito.when(pullRequest.getURL()).thenReturn(new URL(url));
        Mockito.when(pullRequest.getCodebase()).thenReturn(new Codebase(branch));
        Mockito.when(pullRequest.getState()).thenReturn(state);
        Mockito.when(pullRequest.getTitle()).thenReturn(title);
        return pullRequest;
    }

    public static void mockPullRequestReturn(final AphroditeClient client, final PullRequest... requests) {
        Mockito.when(client.getPullRequestAsString(Mockito.anyString())).then(new Answer<PullRequest>() {

            @Override
            public PullRequest answer(InvocationOnMock invocation) throws Throwable {
                final String args = (String) invocation.getArguments()[0];
                for (PullRequest pr : requests) {
                    if (pr.getURL().toString().equals(args))
                        return pr;
                }
                return null;
            }
        });
    }

    public static StreamComponent mockStreamComponent(final String cmpName, final String codeBaseName, final String url) throws URISyntaxException {
        final StreamComponent mockComponent = Mockito.mock(StreamComponent.class);
        final Codebase mockCodebase = Mockito.mock(Codebase.class);

        Mockito.when(mockCodebase.getName()).thenReturn(codeBaseName);
        Mockito.when(mockComponent.getName()).thenReturn(cmpName);
        Mockito.when(mockComponent.getCodebase()).thenReturn(mockCodebase);
        Mockito.when(mockComponent.getRepositoryURL()).thenReturn(new URI(url));
        return mockComponent;
    }

    public static Stream mockStream(final String name, final String[] componentNames, final String[] cbs, final String[] urls) throws URISyntaxException {
        final Stream mock = Mockito.mock(Stream.class);
        Mockito.when(mock.getName()).thenReturn(name);
        String cmpName = null, codeBaseName= null, url = null;
        final List<StreamComponent> components = new ArrayList<>();
        for(int index =0; index<componentNames.length; index++) {
            cmpName = componentNames[index];
            codeBaseName = cbs[index];
            url = urls[index];
            final StreamComponent mockComponent = Mockito.mock(StreamComponent.class);
            //final Codebase mockCodebase = Mockito.mock(Codebase.class);

            //Mockito.when(mockCodebase.getName()).thenReturn(codeBaseName);
            Mockito.when(mockComponent.getName()).thenReturn(cmpName);
            //Mockito.when(mockComponent.getCodebase()).thenReturn(mockCodebase);
            Mockito.when(mockComponent.getCodebase()).thenReturn(new Codebase(codeBaseName));
            Mockito.when(mockComponent.getRepositoryURL()).thenReturn(new URI(url));
            components.add(mockComponent);
        }
        Mockito.when(mock.getAllComponents()).thenReturn(components);
        Mockito.when(mock.getComponent(Mockito.anyString())).then(new Answer<StreamComponent>() {

            @Override
            public StreamComponent answer(InvocationOnMock invocation) throws Throwable {
                final String args = (String) invocation.getArguments()[0];
                for (StreamComponent pr : components) {
                    if (pr.getName().equals(args))
                        return pr;
                }
                return null;
            }
        });
        return mock;
    }

    public static void mockStreamReturn(AphroditeClient mockAphroditeClientIfNeeded, Stream...streams) {
        final List<Stream> allStreams = new ArrayList<>();
        for(Stream s:streams) {
            allStreams.add(s);
        }
        Mockito.when(mockAphroditeClientIfNeeded.getAllStreams()).thenReturn(allStreams);
    }

    public static void mockRepository(PullRequest pullRequest, String url, List<Codebase> codebases) throws MalformedURLException {
        Repository repo = Mockito.mock(Repository.class);
        Mockito.when(repo.getURL()).thenReturn(new URL(url));
        Mockito.when(repo.getCodebases()).thenReturn(codebases);
        Mockito.when(pullRequest.getRepository()).thenReturn(repo);
    }
}
