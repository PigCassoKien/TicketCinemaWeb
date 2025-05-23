package com.example.besrc.ServicesImplement;

import com.example.besrc.Entities.EnumEntities.ESeat;
import com.example.besrc.Entities.EnumEntities.ESeatStatus;
import com.example.besrc.Entities.Seat;
import com.example.besrc.Entities.ShowSeat;
import com.example.besrc.Exception.BadRequestException;
import com.example.besrc.Exception.NotFoundException;
import com.example.besrc.Repository.SeatRepository;
import com.example.besrc.Repository.ShowSeatRepository;
import com.example.besrc.ServerResponse.MyApiResponse;
import com.example.besrc.ServerResponse.ErrorResponse;
import com.example.besrc.ServerResponse.SeatResponse;
import com.example.besrc.ServerResponse.ShowSeatResponse;
import com.example.besrc.Service.SeatService;
import com.example.besrc.requestClient.EditSeatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatServiceImplement implements SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    private ESeat getType(String type) {
        type = type.toUpperCase();
        return switch (type) {
            case "VIP" -> ESeat.VIP;
            case "NORMAL" -> ESeat.NORMAL;
            default -> null;
        };
    }

    private ESeatStatus getStatus(String status) {
        status = status.toUpperCase();
        return switch (status) {
            case "AVAILABLE" -> ESeatStatus.AVAILABLE;
            case "BOOKED" -> ESeatStatus.BOOKED;
            case "UNAVAILABLE" -> ESeatStatus.NOT_AVAILABLE;
            default -> null;
        };
    }

    @Override
    public List<SeatResponse> getAllSeatsFromShow(String showId) {
        List<Seat> seats = seatRepository.findByHallId(showId);
        List<SeatResponse> responses = new ArrayList<>();
        for (Seat seat : seats) {
            responses.add(new SeatResponse(seat));
        }
        return responses;
    }

    @Override
    public List<SeatResponse> getSeat(String showId, int row, int col) {
        Seat seat = seatRepository.findByHallIdAndRowIndexAndColIndex(showId, row, col)
                .orElseThrow(() -> new NotFoundException("Seat not found"));
        return List.of(new SeatResponse(seat));
    }

    @Override
    public MyApiResponse updateSeat(EditSeatRequest request) {
        Seat seat = seatRepository.findByHallIdAndRowIndexAndColIndex(String.valueOf(request.getId()), request.getRow(), request.getCol())
                .orElseThrow(() -> new NotFoundException("Seat not found"));

        ESeat type = this.getType(request.getType());
        if (type == null) {
            return new ErrorResponse("Invalid Type");
        }
        ESeatStatus status = this.getStatus(request.getStatus());
        if (status == null) {
            return new ErrorResponse("Invalid Status");
        }

        seat.setSeatType(type);
        seat.setStatus(status);
        seatRepository.save(seat);
        return new MyApiResponse("OK", 200, "Update seat success");
    }

    @Override
    public boolean isExist(String showId, int row, int col) {
        return seatRepository.findByHallIdAndRowIndexAndColIndex(showId, row, col).isPresent();
    }

    @Override
    public void removeAllSeats(String showId) {
        seatRepository.deleteByHallId(showId);
    }

    @Override
    public MyApiResponse deleteSeat(String showId, int row, int col) {
        Seat seat = seatRepository.findByHallIdAndRowIndexAndColIndex(showId, row, col)
                .orElseThrow(() -> new NotFoundException("Seat not found"));
        seatRepository.delete(seat);
        return new MyApiResponse("OK", 200, "Seat deleted successfully");
    }

    @Override
    public MyApiResponse deleteAllSeats(String showId) {
        seatRepository.deleteByHallId(showId);
        return new MyApiResponse("OK", 200, "All seats deleted for show: " + showId);
    }

    @Override
    public List<ShowSeatResponse> getSeatByStatus(String showId, String status) {
        ESeatStatus seatStatus = getStatus(status);
        if (seatStatus == null) {
            throw new BadRequestException("Invalid seat status: " + status);
        }

        // Lấy danh sách ShowSeat từ bảng ShowSeat theo showId và trạng thái ghế
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndStatus(showId, seatStatus);

        // Chuyển đổi danh sách ShowSeat thành danh sách ShowSeatResponse
        return showSeats.stream()
                .map(ShowSeatResponse::new) // Gọi constructor của ShowSeatResponse
                .collect(Collectors.toList());
    }




    @Override
    public List<ShowSeatResponse> getSeatBySeatType(String showId, ESeat seatType) {
        // Lấy danh sách ShowSeat từ bảng show_seat theo showId và seatType
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeat_SeatType(showId, seatType);

        // Nếu không có ghế nào, trả về danh sách rỗng
        if (showSeats.isEmpty()) {
            return new ArrayList<>();
        }

        // Chuyển đổi danh sách ShowSeat thành danh sách ShowSeatResponse
        return showSeats.stream()
                .map(ShowSeatResponse::new) // Gọi constructor của ShowSeatResponse
                .toList();
    }

    @Override
    public ShowSeatResponse getSeatIndexBySeatIdAndShowId(Long seatId, String showId) {
        ShowSeat showSeat = showSeatRepository.findBySeatIdAndShowId(seatId, showId)
                .orElseThrow(() -> new NotFoundException("Seat not found for given seatId and showId"));

        return new ShowSeatResponse(showSeat);
    }




}