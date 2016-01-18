package com.samsung.multiscreen.msf20.photoshare;

import com.samsung.multiscreen.Search.OnServiceFoundListener;
import com.samsung.multiscreen.Search.OnServiceLostListener;
import com.samsung.multiscreen.Search.OnStartListener;
import com.samsung.multiscreen.Search.OnStopListener;

public interface SearchListener extends OnStartListener, OnStopListener, OnServiceFoundListener, OnServiceLostListener {
}
