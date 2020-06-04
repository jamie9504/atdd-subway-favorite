package wooteco.subway.service.path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wooteco.subway.domain.line.Line;
import wooteco.subway.domain.line.LineRepository;
import wooteco.subway.domain.line.LineStation;
import wooteco.subway.domain.path.PathType;
import wooteco.subway.domain.station.Station;
import wooteco.subway.domain.station.StationRepository;
import wooteco.subway.service.path.dto.PathResponse;
import wooteco.subway.service.station.dto.StationResponse;

@ExtendWith(MockitoExtension.class)
public class PathServiceTest {

	private static final String STATION_NAME1 = "강남역";
	private static final String STATION_NAME2 = "역삼역";
	private static final String STATION_NAME3 = "선릉역";
	private static final String STATION_NAME4 = "양재역";
	private static final String STATION_NAME5 = "양재시민의숲역";
	private static final String STATION_NAME6 = "서울역";

	@Mock
	private StationRepository stationRepository;
	@Mock
	private LineRepository lineRepository;
	@Mock
	private GraphService graphService;

	private PathService pathService;

	private Station station1;
	private Station station2;
	private Station station3;
	private Station station4;
	private Station station5;
	private Station station6;

	private Line line1;
	private Line line2;

	@BeforeEach
	void setUp() {
		pathService = new PathService(stationRepository, lineRepository, graphService);

		station1 = Station.of(STATION_NAME1).withId(1L);
		station2 = Station.of(STATION_NAME2).withId(2L);
		station3 = Station.of(STATION_NAME3).withId(3L);
		station4 = Station.of(STATION_NAME4).withId(4L);
		station5 = Station.of(STATION_NAME5).withId(5L);
		station6 = Station.of(STATION_NAME6).withId(6L);

		line1 = Line.of("2호선", LocalTime.of(5, 30), LocalTime.of(22, 30), 5).withId(1L);
		line1.addLineStation(LineStation.of(null, 1L, 10, 10).withId(1L));
		line1.addLineStation(LineStation.of(1L, 2L, 10, 10).withId(2L));
		line1.addLineStation(LineStation.of(2L, 3L, 10, 10).withId(3L));

		line2 = Line.of("신분당선", LocalTime.of(5, 30), LocalTime.of(22, 30), 5).withId(2L);
		line2.addLineStation(LineStation.of(null, 1L, 10, 10).withId(4L));
		line2.addLineStation(LineStation.of(1L, 4L, 10, 10).withId(5L));
		line2.addLineStation(LineStation.of(4L, 5L, 10, 10).withId(6L));
	}

	@DisplayName("일반적인 상황의 경로 찾기")
	@Test
	void findPath() {
		when(lineRepository.findAll()).thenReturn(Lists.list(line1, line2));
		when(stationRepository.findAllById(anyList()))
			.thenReturn(Lists.list(station3, station2, station1, station4, station5));
		when(stationRepository.findByName(STATION_NAME3)).thenReturn(Optional.of(station3));
		when(stationRepository.findByName(STATION_NAME5)).thenReturn(Optional.of(station5));
		when(graphService.findPath(anyList(), anyLong(), anyLong(), any()))
			.thenReturn(Lists.list(3L, 2L, 1L, 4L, 5L));

		PathResponse pathResponse = pathService
			.findPath(STATION_NAME3, STATION_NAME5, PathType.DISTANCE);

		List<StationResponse> paths = pathResponse.getStations();
		assertThat(paths).hasSize(5);
		assertThat(paths.get(0).getName()).isEqualTo(STATION_NAME3);
		assertThat(paths.get(1).getName()).isEqualTo(STATION_NAME2);
		assertThat(paths.get(2).getName()).isEqualTo(STATION_NAME1);
		assertThat(paths.get(3).getName()).isEqualTo(STATION_NAME4);
		assertThat(paths.get(4).getName()).isEqualTo(STATION_NAME5);
		assertThat(pathResponse.getDistance()).isEqualTo(40);
		assertThat(pathResponse.getDuration()).isEqualTo(40);
	}

	@DisplayName("출발역과 도착역이 같은 경우")
	@Test
	void findPathWithSameSourceAndTarget() {
		assertThrows(RuntimeException.class,
			() -> pathService.findPath(STATION_NAME3, STATION_NAME3, PathType.DISTANCE));
	}

	@DisplayName("출발역과 도착역이 연결이 되지 않은 경우")
	@Test
	void findPathWithNoPath() {
		assertThrows(RuntimeException.class,
			() -> pathService.findPath(STATION_NAME3, STATION_NAME6, PathType.DISTANCE));
	}
}
