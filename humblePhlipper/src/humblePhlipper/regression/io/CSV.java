package humblePhlipper.regression.io;

public class CSV {
    private CSV() {
    }
    public static String toCSV(Jama.Matrix A, boolean endOfLineDelimit, boolean xHeaders, boolean yHeader) {
        StringBuilder csv = new StringBuilder();

        if (xHeaders) {
            csv.append(humblePhlipper.regression.io.Regressors.getCSV());
        }

        if (yHeader) {
            csv.append("\"profitPerHour\"");
        }

        if (xHeaders || yHeader) {
            if (endOfLineDelimit) {
                csv.append(",");
            }
            csv.append("\n");
        }

        for (int i = 0; i < A.getRowDimension(); i++) {
            for (int j = 0; j < A.getColumnDimension(); j++) {
                csv.append(A.get(i,j));
                if (j < A.getColumnDimension() - 1 || (endOfLineDelimit && i < A.getRowDimension() - 1)) {
                    csv.append(",");
                }
            }
            if (i < A.getRowDimension() - 1) {
                csv.append("\n");
            }
        }

        return csv.toString();
    }
}
