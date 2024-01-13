package humblePhlipper.regression;

import Jama.Matrix;
import org.dreambot.api.utilities.Logger;

public class LinearRegression {

    private Matrix Y; // n x 1 input
    private Matrix X; // n x (k + 1) input
    private boolean finiteCorrection; // finite sample correction, n/(n-k-1)
    public int n, k;
    private Matrix XT; // X'
    private Matrix XTXinv; // (X'X)^{-1}
    private Matrix PX; // projection matrix, X(X'X)^{-1}X'
    private Matrix MX; // annihilator matrix, I - PX
    private Matrix Ml; // l = (1, ..., 1)'
    public Matrix BetaHat; // OLS, (X'X)^{-1}X'Y
    public Matrix OmegaHat; // assuming homoskedasticity, (E'E/(n-k-1)) * (X'X)^{-1}
    public Matrix WhiteOmegaHat; // assuming heteroskedasticity (White SEs), n(X'X)^{-1}(X'diag(e1^2, ..., en^2)X)(X'X)^{-1}
    private double SSRu; // sum of squared residuals unrestricted, E'E where E = MX Y
    private double SSRr; // sum of squared residuals restricted, E'E where E = Ml Y
    public double R2; // R^2, 1 - Y'MX Y/l(l'l)l' = 1 - Y'MX Y/(1/n)ll' where l = (1, ..., 1)'
    public double AdjR2; // 1 - (1 - R2)(n - 1)/(n - k -1)

    public LinearRegression(Matrix Y, Matrix X, boolean finiteCorrection) {
        this.Y = Y;
        this.X = X;
        this.finiteCorrection = finiteCorrection;
        this.n = X.getRowDimension();
        this.k = X.getColumnDimension() - 1;
        solve();
    }

    private void solve() {
        this.XT = X.transpose();
        try {
            this.XTXinv = XT.times(X).inverse();
            this.PX = X.times(XTXinv).times(XT);
            this.MX = Matrix.identity(n, n).minus(PX);
            this.Ml = calcMl();
            this.BetaHat = calcBetaHat();
            this.OmegaHat = calcOmegaHat();
            this.WhiteOmegaHat = calcWhiteOmegaHat();
            this.SSRu = calcSSRu();
            this.SSRr = calcSSRr();
            this.R2 = calcR2();
            this.AdjR2 = calcAdjR2();
        }
        catch (RuntimeException e) {
            Logger.log("<Error: Singular X>" + "k = " + k + "</Error>");
        }
    }

    // OLS, BetaHat = (X'X)^{-1}X'Y
    private Matrix calcBetaHat() {
        return XTXinv.times(XT).times(Y);
    }

    // OmegaHat = (E'E/(n-k-1)) * (X'X)^{-1}
    private Matrix calcOmegaHat() {
        Matrix E = MX.times(Y);
        Matrix ET = E.transpose();
        double sumE2 = ET.times(E).get(0,0);
        if (finiteCorrection) {;
            return XTXinv.times(sumE2 / (n - k - 1));
        } else {
            return XTXinv.times(sumE2 / n);
        }
    }

    // White SEs from OmegaHat = (n/n-k-1)(X'X)^{-1}(X'diag(e1^2, ..., en^2)X)(X'X)^{-1}
    private Matrix calcWhiteOmegaHat() {
        Matrix E = MX.times(Y);
        Matrix diagE2 = DiagE2(E);
        Matrix inner = XT.times(diagE2).times(X);
        if (finiteCorrection) {
            return XTXinv.times(inner).times(XTXinv).times((double) (n)/(n-k-1));
        } else {
            return XTXinv.times(inner).times(XTXinv);
        }

    }

    private static Matrix DiagE2(Matrix E) {
        Matrix ET = E.transpose();
        Matrix diagE2 = E.times(ET);
        for (int i = 0; i < diagE2.getRowDimension(); i++) {
            for (int j = 0; j < diagE2.getColumnDimension(); j++) {
                if (i == j) {
                    continue;
                }
                diagE2.set(i,j,0.0);
            }
        }
        return diagE2;
    }

    private Matrix calcMl() {
        Matrix l = new Jama.Matrix(n, 1, 1.0); // l = (1, ..., 1)'
        Matrix lT = l.transpose(); // l'
        Matrix Pl = l.times(lT).times(1.0/n);
        return Jama.Matrix.identity(n, n).times(1.0).minus(Pl);
    }

    private double calcSSRu() {
        Matrix E = MX.times(Y);
        Matrix ET = E.transpose();
        return ET.times(E).get(0,0);
    }

    private double calcSSRr() {
        Matrix E = Ml.times(Y);
        Matrix ET = E.transpose();
        return ET.times(E).get(0,0);
    }

    private double calcR2() {
        return 1.0 - SSRu/SSRr;
    }

    private double calcAdjR2() {
        return 1.0 - (1.0 - R2)*(n - 1.0)/(n - k - 1.0);
    }


    // Regressor t-tests
    public String calcSigStar(int k, boolean White) {
        Double se = (White) ? Math.sqrt(WhiteOmegaHat.get(k, k)) : Math.sqrt(OmegaHat.get(k, k)) ;
        Double t = Math.abs(BetaHat.get(k, 0) / se);
        if (t > 3.290526731492) {
            return "***";
        } else if (t > 2.575829303549) {
            return "**";
        } else if (t > 1.959963984540) {
            return "*";
        }
        return "";
    }

    // F-statistic, [(SSR_r - SSR_u)/k] / [SSR_u/(n-k-1)]
    public double calcFstat() {
        double numerator = (SSRr - SSRu)/k;
        double denominator = (SSRu)/(n-k-1);
        return numerator/denominator;
    }

}
