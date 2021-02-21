package com.example.myruns;

class WekaClassifier {

    public static double classify(Object[] i) {

        return WekaClassifier.N491088a20(i);
    }
    static double N491088a20(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if ((Double) i[0] <= 40.054199) {
            p = WekaClassifier.N7877cd2d1(i);
        } else if ((Double) i[0] > 40.054199) {
            p = WekaClassifier.N277dd4215(i);
        }
        return p;
    }
    static double N7877cd2d1(Object []i) {
        double p = Double.NaN;
        if (i[16] == null) {
            p = 0;
        } else if ((Double) i[16] <= 0.46545) {
            p = 0;
        } else if ((Double) i[16] > 0.46545) {
            p = WekaClassifier.N2b17afa42(i);
        }
        return p;
    }
    static double N2b17afa42(Object []i) {
        double p = Double.NaN;
        if (i[29] == null) {
            p = 0;
        } else if ((Double) i[29] <= 0.269123) {
            p = 0;
        } else if ((Double) i[29] > 0.269123) {
            p = WekaClassifier.N2c3dab303(i);
        }
        return p;
    }
    static double N2c3dab303(Object []i) {
        double p = Double.NaN;
        if (i[6] == null) {
            p = 1;
        } else if ((Double) i[6] <= 1.907262) {
            p = 1;
        } else if ((Double) i[6] > 1.907262) {
            p = WekaClassifier.N6125638d4(i);
        }
        return p;
    }
    static double N6125638d4(Object []i) {
        double p = Double.NaN;
        if (i[4] == null) {
            p = 1;
        } else if ((Double) i[4] <= 3.02855) {
            p = 1;
        } else if ((Double) i[4] > 3.02855) {
            p = 0;
        }
        return p;
    }
    static double N277dd4215(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if ((Double) i[0] <= 624.246411) {
            p = WekaClassifier.N318135e56(i);
        } else if ((Double) i[0] > 624.246411) {
            p = 2;
        }
        return p;
    }
    static double N318135e56(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if ((Double) i[0] <= 56.719164) {
            p = WekaClassifier.N70246cce7(i);
        } else if ((Double) i[0] > 56.719164) {
            p = WekaClassifier.N47f5f8b519(i);
        }
        return p;
    }
    static double N70246cce7(Object []i) {
        double p = Double.NaN;
        if (i[32] == null) {
            p = 0;
        } else if ((Double) i[32] <= 0.021907) {
            p = 0;
        } else if ((Double) i[32] > 0.021907) {
            p = WekaClassifier.N3a57504c8(i);
        }
        return p;
    }
    static double N3a57504c8(Object []i) {
        double p = Double.NaN;
        if (i[3] == null) {
            p = 1;
        } else if ((Double) i[3] <= 3.832826) {
            p = WekaClassifier.N734257fb9(i);
        } else if ((Double) i[3] > 3.832826) {
            p = WekaClassifier.N798caac113(i);
        }
        return p;
    }
    static double N734257fb9(Object []i) {
        double p = Double.NaN;
        if (i[4] == null) {
            p = 1;
        } else if ((Double) i[4] <= 3.059735) {
            p = 1;
        } else if ((Double) i[4] > 3.059735) {
            p = WekaClassifier.N4bd47a6b10(i);
        }
        return p;
    }
    static double N4bd47a6b10(Object []i) {
        double p = Double.NaN;
        if (i[2] == null) {
            p = 0;
        } else if ((Double) i[2] <= 3.937758) {
            p = 0;
        } else if ((Double) i[2] > 3.937758) {
            p = WekaClassifier.N18c52dc311(i);
        }
        return p;
    }
    static double N18c52dc311(Object []i) {
        double p = Double.NaN;
        if (i[7] == null) {
            p = 1;
        } else if ((Double) i[7] <= 1.578648) {
            p = 1;
        } else if ((Double) i[7] > 1.578648) {
            p = WekaClassifier.N1890cba712(i);
        }
        return p;
    }
    static double N1890cba712(Object []i) {
        double p = Double.NaN;
        if (i[3] == null) {
            p = 0;
        } else if ((Double) i[3] <= 2.640379) {
            p = 0;
        } else if ((Double) i[3] > 2.640379) {
            p = 1;
        }
        return p;
    }
    static double N798caac113(Object []i) {
        double p = Double.NaN;
        if (i[19] == null) {
            p = 0;
        } else if ((Double) i[19] <= 0.645964) {
            p = WekaClassifier.N116380b914(i);
        } else if ((Double) i[19] > 0.645964) {
            p = 1;
        }
        return p;
    }
    static double N116380b914(Object []i) {
        double p = Double.NaN;
        if (i[29] == null) {
            p = 0;
        } else if ((Double) i[29] <= 0.145108) {
            p = 0;
        } else if ((Double) i[29] > 0.145108) {
            p = WekaClassifier.N20f90c6315(i);
        }
        return p;
    }
    static double N20f90c6315(Object []i) {
        double p = Double.NaN;
        if (i[26] == null) {
            p = 1;
        } else if ((Double) i[26] <= 0.189777) {
            p = 1;
        } else if ((Double) i[26] > 0.189777) {
            p = WekaClassifier.N3e2a262816(i);
        }
        return p;
    }
    static double N3e2a262816(Object []i) {
        double p = Double.NaN;
        if (i[1] == null) {
            p = 0;
        } else if ((Double) i[1] <= 10.598351) {
            p = WekaClassifier.N22ec498e17(i);
        } else if ((Double) i[1] > 10.598351) {
            p = WekaClassifier.N2f07732218(i);
        }
        return p;
    }
    static double N22ec498e17(Object []i) {
        double p = Double.NaN;
        if (i[32] == null) {
            p = 0;
        } else if ((Double) i[32] <= 0.513667) {
            p = 0;
        } else if ((Double) i[32] > 0.513667) {
            p = 1;
        }
        return p;
    }
    static double N2f07732218(Object []i) {
        double p = Double.NaN;
        if (i[4] == null) {
            p = 1;
        } else if ((Double) i[4] <= 7.261525) {
            p = 1;
        } else if ((Double) i[4] > 7.261525) {
            p = 0;
        }
        return p;
    }
    static double N47f5f8b519(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if ((Double) i[0] <= 314.367548) {
            p = 1;
        } else if ((Double) i[0] > 314.367548) {
            p = WekaClassifier.N28018d0520(i);
        }
        return p;
    }
    static double N28018d0520(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if ((Double) i[0] <= 509.757187) {
            p = 1;
        } else if ((Double) i[0] > 509.757187) {
            p = WekaClassifier.N686101b221(i);
        }
        return p;
    }
    static double N686101b221(Object []i) {
        double p = Double.NaN;
        if (i[18] == null) {
            p = 2;
        } else if ((Double) i[18] <= 2.008769) {
            p = 2;
        } else if ((Double) i[18] > 2.008769) {
            p = WekaClassifier.N7cebf1ff22(i);
        }
        return p;
    }
    static double N7cebf1ff22(Object []i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 2;
        } else if ((Double) i[64] <= 16.827998) {
            p = WekaClassifier.N14339e8723(i);
        } else if ((Double) i[64] > 16.827998) {
            p = 1;
        }
        return p;
    }
    static double N14339e8723(Object []i) {
        double p = Double.NaN;
        if (i[11] == null) {
            p = 1;
        } else if ((Double) i[11] <= 8.936508) {
            p = 1;
        } else if ((Double) i[11] > 8.936508) {
            p = 2;
        }
        return p;
    }
}

