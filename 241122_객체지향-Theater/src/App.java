public class App {
    public static void main(String[] args) {
        TicketOffice ticketOffice = new TicketOffice(100000L, new Ticket(1000L));
        TicketSeller ticketSeller = new TicketSeller(ticketOffice);
        Theater theater = new Theater(ticketSeller);

        Invitation invitation = new Invitation();
        Bag audienceBag = new Bag(invitation, 10000L);
        Audience audience = new Audience(audienceBag);

        theater.enter(audience);
    }
}
