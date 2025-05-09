package com.example.besrc.Service;

import com.example.besrc.Entities.EnumEntities.ESeat;
import com.example.besrc.ServerResponse.MyApiResponse;
import com.example.besrc.ServerResponse.SeatResponse;
import com.example.besrc.ServerResponse.ShowSeatResponse;
import com.example.besrc.requestClient.EditSeatRequest;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface SeatService {
    public List<SeatResponse> getAllSeatsFromShow(String showId);

    public List<SeatResponse> getSeat(String showId, int row, int col);

    public MyApiResponse updateSeat(EditSeatRequest request);

    public boolean isExist(String showId, int row, int col);

    public void removeAllSeats(String showId);

    public MyApiResponse deleteSeat(String showId, int row, int col);

    public MyApiResponse deleteAllSeats(String showId);

    public List<ShowSeatResponse> getSeatByStatus(String showId, String status);
    List<ShowSeatResponse> getSeatBySeatType(String showId, ESeat seatType);

    ShowSeatResponse getSeatIndexBySeatIdAndShowId(Long seatId, String showId);

}
