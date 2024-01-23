package humblePhlipper.regression;

import Jama.Matrix;
import org.dreambot.api.utilities.Logger;

public class LinearRegression {
    private Matrix Y; // n x 1 input
    private Matrix X; // n x (k + 1) input
    public int n, k;
    public Matrix Weights; // Column vector of weights
    public Matrix W; // Weighting matrix
    private Matrix XtWXinv; // Solution (X'WX)^{-1}
    public Matrix B; // Estimated coefficients
    public Matrix E; // Estimated residuals
    public Matrix O; // Variance-covariance matrix of coefficients
    private double SSRu; // sum of squared residuals unrestricted
    private double SSRr; // sum of squared residuals restricted
    public double R2; // R^2, 1 - Y'MX Y/l(l'l)l' = 1 - Y'MX Y/(1/n)ll' where l = (1, ..., 1)'
    public double AdjR2; // 1 - (1 - R2)(n - 1)/(n - k -1)
    public double F; // [(SSR_r - SSR_u)/k] / [SSR_u/(n-k-1)]

    public LinearRegression(Matrix Y, Matrix X) {
        this.Y = Y;
        this.X = X;
        this.n = X.getRowDimension();
        this.k = X.getColumnDimension() - 1;
    }

    private void solve() {
        try {
            this.XtWXinv = X.transpose().times(W).times(X).inverse();
        } catch (RuntimeException e) {
            Logger.log("<Error: Singular X>" + "k = " + k + "</Error>");
            return;
        }

        this.B = XtWXinv.times(X.transpose()).times(W).times(Y);
        this.E = Y.minus(X.times(B));
        this.SSRu = SSRu();
        this.SSRr = SSRr();
        this.R2 = 1.0 - SSRu/SSRr;
        this.AdjR2 = 1.0 - (1.0 - R2)*(n - 1.0)/(n - k - 1.0);
        this.F = ((SSRr - SSRu)/k) / ((SSRu)/(n-k-1));
    }
    public void OLS() {
        this.Weights = new Matrix(n, 1, 1.0);
        this.W = Matrix.identity(n, n);
        solve();
    }
    public void WLS(Matrix Weights) {
        this.Weights = Weights;
        this.W = Diag(Weights);
        solve();
    }
    /*public void CalcOmegaHat(boolean white, boolean finiteCorrection) {
        this.O = (white) ? XtWXinv.times(X.transpose().times(W).times(W.inverse().times(Diag(Pow(E,2)))).times(W.transpose()).times(X)).times(XtWXinv) : XtWXinv.times(SSRu / n);
        this.O = this.O.times((finiteCorrection) ? (double) (n)/(n-k-1) : 1.0);
    }*/
    public void WhiteOmegaHat() {
        Matrix diagE2 = Diag(Pow(E,2));
        Matrix Xt = X.transpose();
        Matrix XtW = Xt.times(W);
        Matrix WX = W.times(X);
        Matrix meat = XtW.times(diagE2).times(WX);
        //Matrix meat = (X.transpose()).times(W).times(diagE2).times(W).times(X);
        this.O = XtWXinv.times(meat).times(XtWXinv);
    }

    public void OmegaHat() {
        this.O = XtWXinv.times(SSRu / n);
    }

    public void FiniteCorrection() {
        this.O = this.O.times((double) (n)/(n-k-1));
    }

    private double SSRu() {
        return E.transpose().times(W).times(E).get(0,0);
    }

    // SSRr in WLS (of which OLS is a corollary) is
    // (W^{1/2}E)'(W^{1/2}E) = E'WE
    // where,
    // E = Y - l(l'w)^{-1}w'Y
    // where,
    // l = (1,...,1)'
    // is an n x 1 vector of 1s and w is an n x 1 vector of weights.
    // In one line,
    // (Y.minus(l(n).times((l(n).transpose().times(Weights)).inverse()).times(Weights.transpose()).times(Y))).transpose().times(W).times(Y.minus(l(n).times((l(n).transpose().times(Weights)).inverse()).times(Weights.transpose()).times(Y))).get(0,0);

    private double SSRr() {
        double sumOfWeights = l(n).transpose().times(Weights).get(0,0);
        double weightedSum = Weights.transpose().times(Y).get(0,0);
        Matrix E = Y.minus(l(n).times(weightedSum/sumOfWeights));
        return E.transpose().times(W).times(E).get(0,0);
        //return (Y.minus(l(n).times((l(n).transpose().times(Weights)).inverse()).times(Weights.transpose()).times(Y))).transpose().times(W).times(Y.minus(l(n).times((l(n).transpose().times(Weights)).inverse()).times(Weights.transpose()).times(Y))).get(0,0);
    }

    private static Matrix Diag(Matrix Vector) {
        Matrix D = new Matrix(Vector.getRowDimension(), Vector.getRowDimension());
        for (int i = 0; i < Vector.getRowDimension(); i++) {
            for (int j = 0; j < Vector.getRowDimension(); j++) {
                if (i == j) {
                    D.set(i,j, Vector.get(i,0));
                } else {
                    D.set(i, j, 0.0);
                }
            }
        }
        return D;
    }
    private static Matrix Pow(Matrix A, double exponent) {
        Matrix P = new Matrix(A.getRowDimension(), A.getColumnDimension());
        for (int i = 0; i < A.getRowDimension(); i++) {
            for (int j = 0; j < A.getColumnDimension(); j++) {
                P.set(i, j, Math.pow(A.get(i, j), exponent));
            }
        }
        return P;
    }

    private static Matrix l(int n) {
        return new Matrix(n,1, 1.0);
    }

}
