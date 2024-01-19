package com.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        TicketServiceImpl ticketService = new TicketServiceImpl();
        TicketTypeRequest[] tk = getTicketTypeRequests();
        ticketService.purchaseTickets(3L, tk);
    }

    private static TicketTypeRequest[] getTicketTypeRequests() {

        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        List<TicketTypeRequest> ticketTypeRequestList = Arrays.asList(ticketTypeRequest, ticketTypeRequest1, ticketTypeRequest2);

        return ticketTypeRequestList.toArray(new TicketTypeRequest[ticketTypeRequestList.size()]);
    }
}
