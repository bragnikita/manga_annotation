export const compFloat = (f1, f2, prec = 0.001) => {
    return Math.abs(f1 - f2) <= prec;
};
export const floatBetween = (fl, less, high) => {
  return (fl >= (less - 0.001) && fl <= high + 0.001)
};
export const doWait = async (sec) => {
    return new Promise((resolve => {
        setTimeout(resolve, sec*1000)
    }))
};
