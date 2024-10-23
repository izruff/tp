package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import seedu.address.model.Model;
import seedu.address.model.person.Grade;
import seedu.address.model.person.GradeList;
import seedu.address.model.person.Person;

/**
 * Aggregation functionalities on grade based on operation and current filtered person list.
 */
public class AggGradeCommand extends Command {
    public static final String COMMAND_WORD = "aggGrade";
    public static final Map<String, Operation> OPERATION_TRANSLATE = Collections.unmodifiableMap(
            Map.of("median", Operation.MEDIAN));


    public enum Operation {
        MEDIAN
    }

    private final Operation operation;
    private final String examName;

    public AggGradeCommand(Operation operation, String examName) {
        requireNonNull(operation);

        this.operation = operation;
        this.examName = examName;
    }

    private CommandResult executeMedian(Model model, SmartList filteredList) {
        requireNonNull(model);
        requireNonNull(filteredList);

        return new CommandResult(String.format("%.2f%%", filteredList.getMedian()));
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);

        SmartList filteredList =
                new SmartList(model.getFilteredPersonList().stream().map(Person::getGradeList).toList());

        if (examName != null) {
            Predicate<Grade> match = (grade) -> grade.getTestName().equalsIgnoreCase(this.examName);

            filteredList =
                    new SmartList(model.getFilteredPersonList().stream().map(Person::getGradeList)
                                          .map(gradeList -> gradeList.filter(match)).toList(), true);
        }

        switch (this.operation) {
        case MEDIAN:
            return executeMedian(model, filteredList);
        }
        throw new IllegalStateException();
    }

    private static class SmartList extends ArrayList<Float> {

        public SmartList(List<GradeList> gradeListList, boolean ignoreWeight) {
            super(
                    gradeListList.stream().map(gradeList -> gradeList.getMap().values().stream()
                            .reduce(0F,
                                    (total, grade) -> grade.getScore() * (!ignoreWeight ?
                                            grade.getWeightage() / 100 : 1),
                                    Float::sum)).toList());
        }

        public SmartList(List<GradeList> gradeListList) {
            this(gradeListList, false);
        }

        public float getMedian() {
            List<Float> list = new ArrayList<>(this);
            int mid = list.size() / 2;

            if (list.isEmpty()) {
                return 0;
            }

            list.sort(Comparator.naturalOrder());

            if (list.size() % 2 == 0) {
                return (list.get(mid - 1) + list.get(mid)) / 2;
            } else {
                return list.get(mid);
            }
        }
    }
}
