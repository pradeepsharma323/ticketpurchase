package uk.gov.dwp.uc.pairtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */
    public static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    TicketPaymentService ticketPaymentService;
    SeatReservationService seatReservationService;

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        boolean isAdult = Arrays.stream(ticketTypeRequests)
                .anyMatch(x -> x.getTicketType()
                        .name()
                        .equalsIgnoreCase("Adult"));

        // calculate no of tickets.
        int noOfTicket = Arrays.stream(ticketTypeRequests).collect(Collectors.summingInt(TicketTypeRequest::getNoOfTickets));
        log.info("total number of ticket is: {}", noOfTicket);

        // ALl Account with an id greater than 0 is valid.
        // purchase will happened only based on ADULT.
        // Only max 20 ticket can booked at a time.
        if (accountId > 0 && isAdult && noOfTicket <= 20) {

            // Calculate the correct amount for the requested tickets & make TicketPaymentServiceImpl request

            int totalTicketPrice = calculateTicketPrice(ticketTypeRequests);
            ticketPaymentService = new TicketPaymentServiceImpl();
            ticketPaymentService.makePayment(accountId, totalTicketPrice);
            log.info("Total Ticket Price is: {}", "£" + totalTicketPrice);

            // Calculates the correct no of seats to reserve & make SeatReservationServiceImpl request

            int seatReserved = calculateNoOfSeatsToBeReserved(ticketTypeRequests);
            seatReservationService = new SeatReservationServiceImpl();
            seatReservationService.reserveSeat(accountId, seatReserved);
            log.info("Total seats reserved is: {}", seatReserved);
        } else {
            throw new InvalidPurchaseException("Invalid purchased: Without Adult ticket can't be purchased");
        }
    }

    private int calculateTicketPrice(TicketTypeRequest[] ticketTypeRequest) {
        // Payment service will called. Infant =£0, Child=£10, Adult =£20;

        final int INFANT_TICKET_PRICE = 0;
        final int CHILD_TICKET_PRICE = 10;
        final int ADULT_TICKET_PRICE = 20;
        int totalAmount = 0;
        for (int i = 0; i < ticketTypeRequest.length; i++) {
            switch (ticketTypeRequest[i].getTicketType()) {
                case INFANT:
                    totalAmount += INFANT_TICKET_PRICE * ticketTypeRequest[i].getNoOfTickets();
                    break;
                case CHILD:
                    totalAmount += CHILD_TICKET_PRICE * ticketTypeRequest[i].getNoOfTickets();
                    break;
                case ADULT:
                    totalAmount += ADULT_TICKET_PRICE * ticketTypeRequest[i].getNoOfTickets();
            }
        }
        return totalAmount;
    }

    private int calculateNoOfSeatsToBeReserved(TicketTypeRequest[] ticketTypeRequest) {

        // seat will be allocated to Adult & Child whereas Infant will sit on lap
        AtomicInteger seatAllocated = new AtomicInteger();
        Arrays.stream(ticketTypeRequest)
                .forEach(tkR -> {
                            if (tkR.getTicketType().name().equalsIgnoreCase("Infant")) {
                                seatAllocated.addAndGet(0);
                            }
                            if (tkR.getTicketType().name().equalsIgnoreCase("Child")) {
                                seatAllocated.addAndGet(tkR.getNoOfTickets());
                            }
                            if (tkR.getTicketType().name().equalsIgnoreCase("Adult")) {
                                seatAllocated.addAndGet(tkR.getNoOfTickets());
                            }

                        }
                );
        return seatAllocated.get();
    }

}
