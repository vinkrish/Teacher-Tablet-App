package in.teacher.model;

import in.teacher.util.ObservableScrollView;

public interface ScrollViewListener {
    void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);
}
